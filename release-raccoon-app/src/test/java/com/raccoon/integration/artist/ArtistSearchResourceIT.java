package com.raccoon.integration.artist;

import com.raccoon.entity.Artist;
import com.raccoon.integration.profile.ArtistSearchDatabaseProfile;
import com.raccoon.scraper.lastfm.RaccoonLastfmApi;
import com.raccoon.scraper.musicbrainz.MusicbrainzClient;
import com.raccoon.search.ArtistSearchResource;

import org.hibernate.search.mapper.orm.session.SearchSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import io.quarkus.test.InjectMock;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import static com.raccoon.Constants.EMAIL_CLAIM;
import static io.restassured.RestAssured.given;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Slf4j
@QuarkusTest
@TestHTTPEndpoint(ArtistSearchResource.class)
@TestTransaction
@TestProfile(value = ArtistSearchDatabaseProfile.class)
class ArtistSearchResourceIT {

    final static String EXISTING_USERNAME = "authenticated";

    @InjectMock
    RaccoonLastfmApi mockRaccoonLastfmApi;
    @InjectMock
    MusicbrainzClient musicbrainzClient;

    @Inject
    EntityManager entityManager;
    @Inject
    SearchSession searchSession;

    @BeforeEach
    @Transactional
    public void setup() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            Artist artist = new Artist();
            artist.setName("name " + i);

            entityManager.persist(artist);
        }

        searchSession.massIndexer(Artist.class).startAndWait();

        when(mockRaccoonLastfmApi.searchArtist(anyString())).thenReturn(Collections.emptyList());
    }

    @Test
    @TestSecurity(user = EXISTING_USERNAME, roles = "user")
    @DisplayName("successful search, should return single artist")
    @OidcSecurity(claims = {
            @Claim(key = EMAIL_CLAIM, value = "user100@mail.com")
    })
    void searchExistingName() {
        given()
                .contentType(ContentType.JSON)
                .param("pattern", "zapp")
                .when().get("/search")
                .then()
                .statusCode(SC_OK)
                .assertThat()
                .body(
                        "artists.size()", is(1),
                        "artists[0].name", equalTo("Zapp Franka")
                );
    }

    @Test
    @TestSecurity(user = EXISTING_USERNAME, roles = "user")
    @DisplayName("successful search, should return two artists")
    @OidcSecurity(claims = {
            @Claim(key = EMAIL_CLAIM, value = "user100@mail.com")
    })
    void searchExistingNameReturnsTwo() throws InterruptedException {
        // Something wrong with the timings here, this seems to be necessary:
        searchSession.massIndexer(Artist.class).startAndWait();

        de.umass.lastfm.Artist lastfmArtist = Mockito.mock(de.umass.lastfm.Artist.class);
        when(lastfmArtist.getName()).thenReturn("Philip Glass");
        when(mockRaccoonLastfmApi.searchArtist("philip")).thenReturn(List.of(lastfmArtist));

        given()
                .contentType(ContentType.JSON)
                .param("pattern", "philip")
                .param("size", "2")
                .when().get("/search")
                .then()
                .statusCode(SC_OK)
                .assertThat()
                .body(
                        "artists.size()", is(3),
                        "artists.name", hasItems( "philip grass", "philip stone"),
                        "artists", hasItem(
                                allOf(
                                        hasEntry("name", "Philip Glass")
                                )
                        )
                );
    }

    @Test
    @TestSecurity(user = EXISTING_USERNAME, roles = "user")
    @DisplayName("successful search, should return no artists")
    void searchNonExistingName() {
        given()
                .contentType(ContentType.JSON)
                .param("pattern", "zrapp")
                .when().get("/search")
                .then()
                .statusCode(SC_OK)
                .assertThat()
                .body("artists.size()", is(0));
    }

}
