package com.raccoon.integration.resource;

import com.github.tomakehurst.wiremock.http.Body;
import com.raccoon.common.WiremockExtensions;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.repository.ReleaseRepository;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.integration.profile.ReleaseScrapeDatabaseProfile;
import com.raccoon.scrape.ReleaseScrapeResource;
import com.raccoon.scrape.ReleaseScrapeWorker;
import com.raccoon.scraper.spotify.SpotifyScraper;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.time.Duration;

import io.quarkus.test.InjectMock;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static io.restassured.RestAssured.given;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Slf4j
@QuarkusTest
@TestHTTPEndpoint(ReleaseScrapeResource.class)
@WithTestResource(WiremockExtensions.class)
@TestProfile(value = ReleaseScrapeDatabaseProfile.class)
@TestTransaction
class ReleaseScrapeResourceIT {

    @Inject
    UserArtistRepository userArtistRepository;
    @InjectMock
    SpotifyScraper mockSpotifyScraper;
    @Inject
    ReleaseRepository releaseRepository;
    @Inject
    ReleaseScrapeWorker releaseScrapeWorker;

    @Test
    void releaseScrape_should_markHasNewRelease() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream resourceAsStream = classLoader.getResourceAsStream("stubs/musicbrainz-releases-with-existent-id.json");
        WiremockExtensions.getWireMockServer()
                .stubFor(
                        get(
                                urlPathMatching("/ws/2/release/.*")
                        ).willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withResponseBody(
                                                Body.fromJsonBytes(resourceAsStream.readAllBytes())
                                        )
                        )
                );
        // When getting for new releases
        given().when()
                .put()
                .then()
                .statusCode(200);

        await("Should complete the scrape before we can query the latest scrape")
                .atMost(Duration.ofSeconds(20))
                .until(() ->
                        nonNull(releaseScrapeWorker.getLatestScrape())
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

    @Test
    void releaseScrape_should_findArtistByMusicbrainzId() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream resourceAsStream = classLoader.getResourceAsStream("stubs/musicbrainz-releases-with-existent-id.json");
        WiremockExtensions.getWireMockServer()
                .stubFor(
                        get(
                                urlPathMatching("/ws/2/release/.*")
                        ).willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withResponseBody(
                                                Body.fromJsonBytes(resourceAsStream.readAllBytes())
                                        )
                        )
                );
        // When getting for new releases
        given().when()
                .put()
                .then()
                .statusCode(200);

        await("Should complete the scrape before we can query the latest scrape")
                .atMost(Duration.ofSeconds(20))
                .until(() ->
                        nonNull(releaseScrapeWorker.getLatestScrape())
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

    @Test
    void releaseScrape_should_work_when_twoReleasesBySameArtistName_with_differentMusicbrainzId() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream resourceAsStream = classLoader.getResourceAsStream("stubs/musicbrainz-two-releases-by-same-artist.json");
        WiremockExtensions.getWireMockServer()
                .stubFor(
                        get(
                                urlPathMatching("/ws/2/release/.*")
                        ).willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withResponseBody(
                                                Body.fromJsonBytes(resourceAsStream.readAllBytes())
                                        )
                        )
                );
        // When getting for new releases
        given().when()
                .put()
                .then()
                .statusCode(200);

        await("Should complete the scrape before we can query the latest scrape")
                .atMost(Duration.ofSeconds(20))
                .until(() ->
                        nonNull(releaseScrapeWorker.getLatestScrape())
                                && releaseScrapeWorker.getLatestScrape().getIsComplete()
                );

        assertThat(releaseRepository.count())
                .as("3 results in the response (1 mocked above + 2 from stub.json)")
                .isGreaterThanOrEqualTo(3);
    }

}
