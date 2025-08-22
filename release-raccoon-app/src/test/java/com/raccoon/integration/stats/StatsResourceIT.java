package com.raccoon.integration.stats;

import com.raccoon.entity.Artist;
import com.raccoon.entity.ArtistRelease;
import com.raccoon.entity.Release;
import com.raccoon.entity.repository.ArtistReleaseRepository;
import com.raccoon.entity.repository.ArtistRepository;
import com.raccoon.entity.repository.ReleaseRepository;
import com.raccoon.stats.StatsResource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

@QuarkusTest
@TestHTTPEndpoint(StatsResource.class)
class StatsResourceIT {

    @Inject
    ArtistRepository artistRepository;

    @Inject
    ReleaseRepository releaseRepository;

    @Inject
    ArtistReleaseRepository artistReleaseRepository;

    @BeforeEach
    @TestTransaction
    void setUp() {
        releaseRepository.deleteAll();
        artistRepository.deleteAll();
    }

    @Test
    @TestTransaction
    void getReleaseCount_should_return_correct_count_when_releases_exist() {
        Artist artist1 = new Artist();
        artist1.setName("Test Artist 1");
        artist1.setSpotifyUri("spotify:artist:123");
        artistRepository.persist(artist1);

        Artist artist2 = new Artist();
        artist2.setName("Test Artist 2");
        artist2.setSpotifyUri("spotify:artist:456");
        artistRepository.persist(artist2);

        Release release1 = new Release();
        release1.setName("Test Release 1");
        release1.setReleasedOn(LocalDate.now());
        release1.setSpotifyUri("spotify:album:111");
        releaseRepository.persist(release1);
        
        ArtistRelease ar1 = new ArtistRelease();
        ar1.setArtist(artist1);
        ar1.setRelease(release1);
        artistReleaseRepository.persist(ar1);

        Release release2 = new Release();
        release2.setName("Test Release 2");
        release2.setReleasedOn(LocalDate.now());
        release2.setSpotifyUri("spotify:album:222");
        releaseRepository.persist(release2);
        
        ArtistRelease ar2 = new ArtistRelease();
        ar2.setArtist(artist2);
        ar2.setRelease(release2);
        artistReleaseRepository.persist(ar2);

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/releases/count")
                .then()
                .statusCode(SC_OK)
                .body("count", is(2));
    }

    @Test
    @TestTransaction
    void getArtistCount_should_return_correct_count_when_artists_exist() {
        Artist artist1 = new Artist();
        artist1.setName("Test Artist 1");
        artist1.setSpotifyUri("spotify:artist:123");
        artistRepository.persist(artist1);

        Artist artist2 = new Artist();
        artist2.setName("Test Artist 2");
        artist2.setSpotifyUri("spotify:artist:456");
        artistRepository.persist(artist2);

        Artist artist3 = new Artist();
        artist3.setName("Test Artist 3");
        artist3.setSpotifyUri("spotify:artist:789");
        artistRepository.persist(artist3);

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/artists/count")
                .then()
                .statusCode(SC_OK)
                .body("count", is(3));
    }

    @Test
    @TestTransaction
    void getReleaseCount_should_return_zero_when_no_releases() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/releases/count")
                .then()
                .statusCode(SC_OK)
                .body("count", is(0));
    }

    @Test
    @TestTransaction
    void getArtistCount_should_return_zero_when_no_artists() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/artists/count")
                .then()
                .statusCode(SC_OK)
                .body("count", is(0));
    }

    @Test
    @TestTransaction
    void getAllStats_should_return_both_counts_when_data_exists() {
        Artist artist1 = new Artist();
        artist1.setName("Test Artist 1");
        artist1.setSpotifyUri("spotify:artist:123");
        artistRepository.persist(artist1);

        Artist artist2 = new Artist();
        artist2.setName("Test Artist 2");
        artist2.setSpotifyUri("spotify:artist:456");
        artistRepository.persist(artist2);

        Release release1 = new Release();
        release1.setName("Test Release 1");
        release1.setReleasedOn(LocalDate.now());
        release1.setSpotifyUri("spotify:album:111");
        releaseRepository.persist(release1);
        
        ArtistRelease ar1 = new ArtistRelease();
        ar1.setArtist(artist1);
        ar1.setRelease(release1);
        artistReleaseRepository.persist(ar1);

        Release release2 = new Release();
        release2.setName("Test Release 2");
        release2.setReleasedOn(LocalDate.now());
        release2.setSpotifyUri("spotify:album:222");
        releaseRepository.persist(release2);
        
        ArtistRelease ar2 = new ArtistRelease();
        ar2.setArtist(artist2);
        ar2.setRelease(release2);
        artistReleaseRepository.persist(ar2);

        Release release3 = new Release();
        release3.setName("Test Release 3");
        release3.setReleasedOn(LocalDate.now());
        release3.setSpotifyUri("spotify:album:333");
        releaseRepository.persist(release3);
        
        ArtistRelease ar3a = new ArtistRelease();
        ar3a.setArtist(artist1);
        ar3a.setRelease(release3);
        artistReleaseRepository.persist(ar3a);
        
        ArtistRelease ar3b = new ArtistRelease();
        ar3b.setArtist(artist2);
        ar3b.setRelease(release3);
        artistReleaseRepository.persist(ar3b);

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("")
                .then()
                .statusCode(SC_OK)
                .body("releaseCount", is(3))
                .body("artistCount", is(2));
    }

    @Test
    @TestTransaction
    void getAllStats_should_return_zeros_when_no_data() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("")
                .then()
                .statusCode(SC_OK)
                .body("releaseCount", is(0))
                .body("artistCount", is(0));
    }

    @Test
    @TestTransaction
    void counts_should_be_independent_when_different_entities() {
        Artist artist = new Artist();
        artist.setName("Test Artist");
        artist.setSpotifyUri("spotify:artist:123");
        artistRepository.persist(artist);

        Release release1 = new Release();
        release1.setName("Release 1");
        release1.setReleasedOn(LocalDate.now());
        release1.setSpotifyUri("spotify:album:111");
        releaseRepository.persist(release1);
        
        ArtistRelease ar1 = new ArtistRelease();
        ar1.setArtist(artist);
        ar1.setRelease(release1);
        artistReleaseRepository.persist(ar1);

        Release release2 = new Release();
        release2.setName("Release 2");
        release2.setReleasedOn(LocalDate.now());
        release2.setSpotifyUri("spotify:album:222");
        releaseRepository.persist(release2);
        
        ArtistRelease ar2 = new ArtistRelease();
        ar2.setArtist(artist);
        ar2.setRelease(release2);
        artistReleaseRepository.persist(ar2);

        long artistCount = artistRepository.count();
        long releaseCount = releaseRepository.count();

        assertThat(artistCount).isEqualTo(1);
        assertThat(releaseCount).isEqualTo(2);

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/artists/count")
                .then()
                .statusCode(SC_OK)
                .body("count", is(1));

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/releases/count")
                .then()
                .statusCode(SC_OK)
                .body("count", is(2));

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("")
                .then()
                .statusCode(SC_OK)
                .body("releaseCount", is(2))
                .body("artistCount", is(1));
    }
}