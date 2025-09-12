package com.raccoon.integration.artist;

import com.raccoon.entity.Artist;
import com.raccoon.integration.profile.ArtistSearchDatabaseProfile;
import com.raccoon.scraper.lastfm.RaccoonLastfmApi;
import com.raccoon.scraper.musicbrainz.MusicbrainzClient;
import com.raccoon.search.ArtistSearchResource;
import com.raccoon.search.dto.SearchResultArtistDto;
import com.raccoon.search.impl.MusicbrainzSearcher;

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
import io.quarkus.test.security.oidc.OidcSecurity;
import io.quarkus.test.security.oidc.UserInfo;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Slf4j
@QuarkusTest
@TestHTTPEndpoint(ArtistSearchResource.class)
@TestTransaction
@TestProfile(value = ArtistSearchDatabaseProfile.class)
class ArtistSearchResourceIT {

    static final String EXISTING_USERNAME = "authenticated";

    @InjectMock
    RaccoonLastfmApi mockRaccoonLastfmApi;
    @InjectMock
    MusicbrainzClient musicbrainzClient;
    @InjectMock
    MusicbrainzSearcher mockMusicbrainzSearcher;

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
    @OidcSecurity(userinfo = {
            @UserInfo(key = "email", value = "user100@mail.com")
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
    @OidcSecurity(userinfo = {
            @UserInfo(key = "email", value = "user100@mail.com")
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

    @Test
    @TestSecurity(user = EXISTING_USERNAME, roles = "user")
    @OidcSecurity(userinfo = {
            @UserInfo(key = "email", value = "user100@mail.com")
    })
    void search_followedArtist_should_set_followedByUser() throws InterruptedException {
        // Something wrong with the timings here, this seems to be necessary:
        searchSession.massIndexer(Artist.class).startAndWait();

        when(mockRaccoonLastfmApi.searchArtist("led")).thenReturn(List.of());

        given()
                .contentType(ContentType.JSON)
                .param("pattern", "zeppeling")
                .param("size", "2")
                .when().get("/search")
                .then()
                .statusCode(SC_OK)
                .assertThat()
                .body(
                        "artists.size()", is(1),
                        "artists[0].name", equalTo("led zeppeling"),
                        "artists[0].followedByUser", equalTo(true)
                );
    }

    @Test
    @TestSecurity(user = EXISTING_USERNAME, roles = "user")
    @OidcSecurity(userinfo = {
            @UserInfo(key = "email", value = "user100@mail.com")
    })
    void search_followedArtist_should_set_followedByUser_when_externalSearchServicesReturn() throws InterruptedException {
        // Something wrong with the timings here, this seems to be necessary:
        searchSession.massIndexer(Artist.class).startAndWait();

        de.umass.lastfm.Artist lastfmArtist = Mockito.mock(de.umass.lastfm.Artist.class);
        when(lastfmArtist.getName()).thenReturn("led zeppeling");
        when(mockRaccoonLastfmApi.searchArtist(anyString())).thenReturn(List.of(lastfmArtist));

        when(lastfmArtist.getName()).thenReturn("led zeppeling");
        when(mockMusicbrainzSearcher.searchArtist(any(), any()))
                .thenReturn(
                        List.of(
                            new SearchResultArtistDto(null, "led zeppeling", null, null, "musicbrainzId", false, 0)
                        )
                );

        given()
                .contentType(ContentType.JSON)
                .param("pattern", "zeppeling")
                .param("size", "2")
                .when().get("/search")
                .then()
                .statusCode(SC_OK)
                .assertThat()
                .body(
                        "artists.size()", is(1),
                        "artists[0].name", equalTo("led zeppeling"),
                        "artists[0].followedByUser", equalTo(true)
                );
    }
}
