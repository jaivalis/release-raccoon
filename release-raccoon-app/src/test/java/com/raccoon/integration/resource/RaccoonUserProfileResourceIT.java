package com.raccoon.integration.resource;

import com.raccoon.dto.ArtistDto;
import com.raccoon.entity.Artist;
import com.raccoon.entity.repository.ArtistReleaseRepository;
import com.raccoon.entity.repository.ArtistRepository;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.search.dto.SearchResultArtistDto;
import com.raccoon.user.RedirectConfig;
import com.raccoon.user.UserProfileResource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.InjectMock;
import io.quarkus.test.Mock;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.smallrye.config.SmallRyeConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import static com.raccoon.Constants.EMAIL_CLAIM;
import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_TEMPORARY_REDIRECT;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestHTTPEndpoint(UserProfileResource.class)
@QuarkusTestResource(H2DatabaseTestResource.class)
@TestTransaction
class RaccoonUserProfileResourceIT {

    static final String EXISTING_USERNAME = "the coon";

    @Inject
    MockMailbox mockMailbox;
    @Inject
    UserRepository userRepository;
    @Inject
    ArtistRepository artistRepository;
    @Inject
    UserArtistRepository userArtistRepository;
    @Inject
    ArtistReleaseRepository artistReleaseRepository;

    @Inject
    SmallRyeConfig smallRyeConfig;

    @InjectMock
    RedirectConfig redirectConfig;

    @ApplicationScoped
    @Mock
    RedirectConfig featuresConfig() {
        return smallRyeConfig.getConfigMapping(RedirectConfig.class);
    }

    @BeforeEach
    @Transactional
    public void setup() {
        mockMailbox.clear();
    }

    @Test
    @TestSecurity(user = EXISTING_USERNAME, roles = "user")
    @OidcSecurity(claims = {
            @Claim(key = EMAIL_CLAIM, value = "getProfileOnce@gmail.com")
    })
    @DisplayName("successful get, should send welcome mail")
    void getProfileOnce() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get()
                .then()
                .statusCode(SC_OK);

        assertThat(mockMailbox.getMailsSentTo("getProfileOnce@gmail.com"))
                .hasSize(1);
    }

    @Test
    @TestSecurity(user = EXISTING_USERNAME, roles = "user")
    @OidcSecurity(claims = {
            @Claim(key = EMAIL_CLAIM, value = "getProfileOnce@gmail.com")
    })
    @DisplayName("get should redirect when url in whitelist")
    void getWithRedirect_should_redirect_when_urlInWhitelist() {
        String redirectUrl = "https://mock.url.from.whitelist";
        when(redirectConfig.whitelistedUrls()).thenReturn(Optional.of(List.of(redirectUrl)));

        Response response = given()
                .queryParam("redirectUrl", redirectUrl)
                .redirects().follow(false)
                .contentType(ContentType.JSON)
                .when()
                .get()
                .then()
                .statusCode(SC_TEMPORARY_REDIRECT)
                .extract()
                .response();

        assertThat(response.getHeader("Location"))
                .isEqualTo(redirectUrl);
    }

    @Test
    @TestSecurity(user = EXISTING_USERNAME, roles = "user")
    @OidcSecurity(claims = {
            @Claim(key = EMAIL_CLAIM, value = "getProfileOnce@gmail.com")
    })
    @DisplayName("get should not redirect when url not in whitelist")
    void getWithRedirect_should_not_redirect_when_urlNotInWhitelist() {
        String redirectUrl = "https://mock.url.from.whitelist";
        when(redirectConfig.whitelistedUrls()).thenReturn(Optional.of(List.of("not" + redirectUrl)));

        Response response = given()
                .queryParam("redirectUrl", redirectUrl)
                .contentType(ContentType.JSON)
                .when()
                .get()
                .then()
                .statusCode(SC_OK)
                .extract()
                .response();

        assertThat(response.getHeader("Location"))
                .isNull();
    }

    @Test
    @TestSecurity(user = EXISTING_USERNAME, roles = "user")
    @OidcSecurity(claims = {
            @Claim(key = EMAIL_CLAIM, value = "getProfileOnce@gmail.com")
    })
    @DisplayName("get should not redirect when url not in whitelist")
    void getWithRedirect_should_not_redirect_when_whitelistEmpty() {
        String redirectUrl = "https://mock.url.from.whitelist";
        when(redirectConfig.whitelistedUrls()).thenReturn(Optional.empty());

        Response response = given()
                .queryParam("redirectUrl", redirectUrl)
                .contentType(ContentType.JSON)
                .when()
                .get()
                .then()
                .statusCode(SC_OK)
                .extract()
                .response();

        assertThat(response.getHeader("Location"))
                .isNull();
    }

    @Test
    @TestSecurity(user = EXISTING_USERNAME, roles = "user")
    @OidcSecurity(claims = {
            @Claim(key = EMAIL_CLAIM, value = "getProfileTwice@gmail.com")
    })
    @DisplayName("successful get, called twice should send single welcome mail")
    void getProfileTwice() {
        // create the raccoonUser
        given()
                .contentType(ContentType.JSON)
                .when().get()
                .then()
                .statusCode(SC_OK);
        given()
                .contentType(ContentType.JSON)
                .when().get()
                .then()
                .statusCode(SC_OK);

        assertEquals(1, mockMailbox.getMailsSentTo("getProfileTwice@gmail.com").size());
    }

    @Test
    @TestSecurity(user = EXISTING_USERNAME, roles = "user")
    @OidcSecurity(claims = {
            @Claim(key = "email", value = "raccoonUser@gmail.com")
    })
    @DisplayName("follow artist")
    void followArtist() {
        // create the raccoonUser
        given()
                .contentType(ContentType.JSON)
                .when().get()
                .then()
                .statusCode(SC_OK);

        // follow the artist
        SearchResultArtistDto artistDto = SearchResultArtistDto.builder()
                .name("name")
                .spotifyUri("spotifyUri")
                .lastfmUri("lastfmUri")
                .build();
        given()
                .contentType(ContentType.JSON)
                .with().body(
                        artistDto
                )
                .when().post("/follow")
                .then()
                .statusCode(SC_NO_CONTENT);

        Long userId = userRepository.findByEmail("raccoonUser@gmail.com").id;
        assertEquals(1, userArtistRepository.findByUserId(userId).size());
        Artist followedArtist = userArtistRepository.findByUserId(userId).get(0).getArtist();
        assertEquals("name", followedArtist.getName());
        assertEquals("spotifyUri", followedArtist.getSpotifyUri());
        assertEquals("lastfmUri", followedArtist.getLastfmUri());
    }

    @Test
    @TestSecurity(user = EXISTING_USERNAME, roles = "user")
    @OidcSecurity(claims = {
            @Claim(key = "email", value = "raccoonUser@gmail.com")
    })
    @DisplayName("DELETE `/me/artist` deletes UserArtist association")
    void unfollowArtist() {
        // create the raccoonUser
        given()
                .contentType(ContentType.JSON)
                .when().get()
                .then()
                .statusCode(SC_OK);

        given()
                .contentType(ContentType.JSON)
                .when().delete("/unfollow/1")
                .then()
                .statusCode(SC_NO_CONTENT);
    }

    @Test
    @TestSecurity(user = EXISTING_USERNAME, roles = "user")
    @OidcSecurity(claims = {
            @Claim(key = "email", value = "raccoonUser@gmail.com")
    })
    @DisplayName("enable-services")
    void enableServices() {
        // create the raccoonUser
        given()
                .contentType(ContentType.JSON)
                .when().get()
                .then()
                .statusCode(SC_OK);

        given()
                .contentType(ContentType.JSON)
                .param("lastfmUsername", "username")
                .param("enableSpotify", false)
                .when().get("/enable-services/")
                .then()
                .statusCode(SC_OK);
    }

    @Test
    @TestSecurity(user = EXISTING_USERNAME, roles = "user")
    @OidcSecurity(claims = {
            @Claim(key = EMAIL_CLAIM, value = "raccoonUser@gmail.com")
    })
    @DisplayName("GET `/me/followed-artists` returns list of Artists")
    void getUserArtists() {
        // create the raccoonUser
        given()
                .contentType(ContentType.JSON)
                .when().get()
                .then()
                .statusCode(SC_OK);
        // follow the artist
        ArtistDto artistDto = ArtistDto.builder()
                .name("name")
                .spotifyUri("spotifyUri")
                .lastfmUri("lastfmUri")
                .build();
        given()
                .contentType(ContentType.JSON)
                .with().body(
                        artistDto
                )
                .when().post("/follow")
                .then()
                .statusCode(SC_NO_CONTENT);

        // get followed artists
        List<ArtistDto> list = given()
                .contentType(ContentType.JSON)
                .when().get("followed-artists")
                .then()
                .statusCode(SC_OK)
                .extract().body().jsonPath().getList("rows", ArtistDto.class);

        assertThat(list).hasSize(1);
        assertEquals(artistDto.getName(), list.get(0).getName());
        assertEquals(artistDto.getSpotifyUri(), list.get(0).getSpotifyUri());
        assertEquals(artistDto.getLastfmUri(), list.get(0).getLastfmUri());
    }

    @Test
    @DisplayName("no bearer token, unauthorized")
    void unauthorized() {
        given()
                .contentType(ContentType.JSON)
                .when().get()
                .then()
                .statusCode(SC_UNAUTHORIZED);
    }

}
