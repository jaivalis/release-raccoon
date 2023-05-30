package com.raccoon;

import com.raccoon.common.ElasticSearchTestResource;
import com.raccoon.common.WiremockExtensions;
import com.raccoon.entity.Release;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.scrape.ReleaseScrapeResource;
import com.raccoon.scrape.dto.ReleaseScrapeResponse;
import com.raccoon.scraper.spotify.SpotifyScraper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import javax.inject.Inject;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;

import static io.quarkus.test.junit.QuarkusMock.installMockForType;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.StringContains.containsString;
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
@QuarkusTestResource(ElasticSearchTestResource.class)
@QuarkusTestResource(WiremockExtensions.class)
class ReleaseScrapeResourceIT {

    @Inject
    UserArtistRepository userArtistRepository;
    @InjectMock
    SpotifyScraper mockSpotifyScraper;

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
        ReleaseScrapeResponse result = given().when()
                .put()
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(containsString(scrapedRelease.getName()))
                .extract()
                .as(ReleaseScrapeResponse.class);

        assertThat(result.releases())
                .as("23 results in the response (1 mocked above + 22 from stub.json)")
                .hasSize(23);

        var uaOptional = userArtistRepository.findByUserIdArtistIdOptional(400L, 400L);
        assertThat(uaOptional).isPresent();
        assertThat(uaOptional.get().hasNewRelease)
                .as("UserArtist association `hasNewRelease` flag is true")
                .isTrue();
    }

}
