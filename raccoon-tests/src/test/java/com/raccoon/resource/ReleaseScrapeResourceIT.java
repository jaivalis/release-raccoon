package com.raccoon.resource;

import com.raccoon.common.WiremockExtensions;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.repository.ReleaseRepository;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.profile.ReleaseScrapeDatabaseProfile;
import com.raccoon.scrape.ReleaseScrapeResource;
import com.raccoon.scrape.ReleaseScrapeWorker;
import com.raccoon.scraper.spotify.SpotifyScraper;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Objects;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import jakarta.transaction.UserTransaction;
import lombok.extern.slf4j.Slf4j;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Slf4j
@QuarkusTest
@TestHTTPEndpoint(ReleaseScrapeResource.class)
@QuarkusTestResource(WiremockExtensions.class)
@TestProfile(value = ReleaseScrapeDatabaseProfile.class)
class ReleaseScrapeResourceIT {

    @Inject
    UserArtistRepository userArtistRepository;
    @InjectMock
    SpotifyScraper mockSpotifyScraper;
    @Inject
    ReleaseRepository releaseRepository;
    @Inject
    ReleaseScrapeWorker releaseScrapeWorker;

    @Inject
    UserTransaction transaction;

    @Test
    void releaseScrape_should_markHasNewRelease() throws HeuristicRollbackException, SystemException, HeuristicMixedException, RollbackException, NotSupportedException {
        // The transaction used within the test scope did not contain the changed flag unless this
        // was set per:
        //                  https://github.com/quarkusio/quarkus/issues/6536#issuecomment-699649094
        transaction.begin();
        transaction.commit();

        // When getting for new releases
        given().when()
                .put()
                .then()
                .statusCode(200);

        await("Should complete the scrape before we can query the latest scrape")
                .atMost(Duration.ofSeconds(20))
                .until(() ->
                        Objects.nonNull(releaseScrapeWorker.getLatestScrape())
                        && releaseScrapeWorker.getLatestScrape().getIsComplete()
                );

        assertThat(releaseRepository.count())
                .as("23 results in the response (1 mocked above + 22 from stub.json)")
                .isGreaterThanOrEqualTo(23);

        var uaOptional = userArtistRepository.findByUserIdArtistIdOptional(100L, 100L);
        assertThat(uaOptional).isPresent()
                .get()
                .extracting(UserArtist::getHasNewRelease)
                .as("UserArtist association `hasNewRelease` should be marked true")
                .isEqualTo(Boolean.TRUE);
    }

}
