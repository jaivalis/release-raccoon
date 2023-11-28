package com.raccoon.resource;

import com.raccoon.common.WiremockExtensions;
import com.raccoon.entity.Release;
import com.raccoon.entity.repository.ReleaseRepository;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.scrape.ReleaseScrapeResource;
import com.raccoon.scrape.ReleaseScrapeWorker;
import com.raccoon.scraper.spotify.SpotifyScraper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import jakarta.transaction.UserTransaction;

import static io.quarkus.test.junit.QuarkusMock.installMockForType;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Comments are not allowed in the `import-test.sql` file so clarifying here. These tests depend on
 * the following data being present in the db:
 *
 * INSERT INTO
 *     Artist
 *     (artistId, name)
 * VALUES
 *     (100, 'existentArtist');
 *
 * INSERT INTO
 *     Releases
 *     (releaseId, name, type)
 * VALUES
 *     (100, 'newRelease', 'ALBUM');
 *
 * INSERT INTO
 *     RaccoonUser
 *     (id, email)
 * VALUES
 *     (100, 'user200@mail.com');
 *
 * INSERT INTO
 *     UserArtist
 *     (user_id, artist_id)
 * VALUES
 *     (100, 100);
 *
 * INSERT INTO
 *     ArtistRelease
 * (artist_id, release_id)
 * VALUES
 *     (100, 100);
 */
@QuarkusTest
@TestHTTPEndpoint(ReleaseScrapeResource.class)
@QuarkusTestResource(WiremockExtensions.class)
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

    @BeforeEach
    public void setup() {
        installMockForType(mockSpotifyScraper, SpotifyScraper.class);
    }

    @Test
    @DisplayName("new releases, should set UserArtist.hasNewRelease to true")
    void releaseScrape_should_markHasNewRelease() throws InterruptedException, HeuristicRollbackException, SystemException, HeuristicMixedException, RollbackException, NotSupportedException {
        // The transaction used within the test scope did not contain the changed flag unless this
        // was set per:
        //                  https://github.com/quarkusio/quarkus/issues/6536#issuecomment-699649094
        transaction.begin();
        transaction.commit();

        // Given spotifyScraper returns a Release by an artist followed by our raccoonUser
        Release scrapedRelease = Release.findById(400L);
        when(mockSpotifyScraper.scrapeReleases(any()))
                .thenReturn(Set.of(scrapedRelease));

        // When getting for new releases
        given().when()
                .put()
                .then()
                .statusCode(200);

        await("Should complete the scrape before we can query the latest scrape").atMost(Duration.ofSeconds(10))
                .until(() -> !releaseScrapeWorker.isRunning());

        assertThat(releaseRepository.count())
                .as("23 results in the response (1 mocked above + 22 from stub.json)")
                .isGreaterThanOrEqualTo(23);

        var uaOptional = userArtistRepository.findByUserIdArtistIdOptional(400L, 400L);
        assertThat(uaOptional).isPresent();
        assertThat(uaOptional.get().hasNewRelease)
                .as("UserArtist association `hasNewRelease` flag is true")
                .isTrue();
    }

}
