package com.raccoon;

import com.raccoon.common.ElasticSearchTestResource;
import com.raccoon.common.WiremockExtensions;
import com.raccoon.entity.Release;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.release.ReleaseScrapeResource;
import com.raccoon.scraper.spotify.SpotifyScraper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Set;

import javax.inject.Inject;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;

import static io.quarkus.test.junit.QuarkusMock.installMockForType;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
 *     User
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
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestHTTPEndpoint(ReleaseScrapeResource.class)
@QuarkusTestResource(ElasticSearchTestResource.class)
@QuarkusTestResource(WiremockExtensions.class)
class ReleaseScrapeResourceIT {

    @Inject
    UserArtistRepository userArtistRepository;
    @InjectMock
    SpotifyScraper mockSpotifyScraper;

    @BeforeEach
    public void setup() {
        installMockForType(mockSpotifyScraper, SpotifyScraper.class);
    }

    @Test
    @Order(1)
    @DisplayName("new releases, should set UserArtist.hasNewRelease to true")
    void releaseScrape_should_mark_hasNewRelease() throws InterruptedException {
        // Given spotifyScraper returns a Release by an artist followed by our user
        Release scrapedRelease = Release.findById(400L);
        when(mockSpotifyScraper.scrapeReleases(any())).thenReturn(
                Set.of(scrapedRelease)
        );

        // When getting for new releases
        Release[] result = given().when()
                .get()
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(containsString(scrapedRelease.getName()))
                .extract()
                // todo: define a Dto for this
                .as(Release[].class);
        var userArtistAssociations = userArtistRepository.findByUserId(400L);

        // Then the UserArtist association `hasNewRelease` flag is true
        assertEquals(1, userArtistAssociations.size());
        assertThat((userArtistAssociations.get(0)).getHasNewRelease()).isTrue();
        // and 23 results in the response (1 mocked above + 22 from stub.json)
        assertThat(result).hasSize(23);
    }

}
