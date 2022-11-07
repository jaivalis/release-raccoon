package com.raccoon;

import com.raccoon.common.ElasticSearchTestResource;
import com.raccoon.entity.Artist;
import com.raccoon.entity.repository.ArtistReleaseRepository;
import com.raccoon.entity.repository.ArtistRepository;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.search.dto.ArtistDto;
import com.raccoon.user.UserProfileResource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import javax.inject.Inject;
import javax.transaction.Transactional;

import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.restassured.http.ContentType;

import static com.raccoon.Constants.EMAIL_CLAIM;
import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@Testcontainers
@TestHTTPEndpoint(UserProfileResource.class)
@QuarkusTestResource(ElasticSearchTestResource.class)
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

    @BeforeEach
    @Transactional
    public void setup() {
        mockMailbox.clear();
    }

    @Test
    @TestTransaction
    @TestSecurity(user = EXISTING_USERNAME, roles = "user")
    @OidcSecurity(claims = {
            @Claim(key = EMAIL_CLAIM, value = "getProfileOnce@gmail.com")
    })
    @DisplayName("successful get, should send welcome mail")
    void getProfileOnce() {
        given()
                .contentType(ContentType.JSON)
                .when().get()
                .then()
                .statusCode(SC_OK);

        assertEquals(1, mockMailbox.getMessagesSentTo("getProfileOnce@gmail.com").size());
    }

    @Test
    @TestTransaction
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

        assertEquals(1, mockMailbox.getMessagesSentTo("getProfileTwice@gmail.com").size());
    }

    @Test
    @TestTransaction
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

        Long userId = userRepository.findByEmail("raccoonUser@gmail.com").id;
        assertEquals(1, userArtistRepository.findByUserId(userId).size());
        Artist followedArtist = userArtistRepository.findByUserId(userId).get(0).getArtist();
        assertEquals("name", followedArtist.getName());
        assertEquals("spotifyUri", followedArtist.getSpotifyUri());
        assertEquals("lastfmUri", followedArtist.getLastfmUri());
    }

    @Test
    @TestTransaction
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
    @TestTransaction
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
    @TestTransaction
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

        assertEquals(1, list.size());
        assertEquals(artistDto.getName(), list.get(0).getName());
        assertEquals(artistDto.getSpotifyUri(), list.get(0).getSpotifyUri());
        assertEquals(artistDto.getLastfmUri(), list.get(0).getLastfmUri());
    }

    @Test
    @TestTransaction
    @DisplayName("no bearer token, unauthorized")
    void unauthorized() {
        given()
                .contentType(ContentType.JSON)
                .when().get()
                .then()
                .statusCode(SC_UNAUTHORIZED);
    }

}
