package com.raccoon;

import com.raccoon.common.ElasticSearchTestResource;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.user.UserProfileResource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.inject.Inject;
import javax.transaction.Transactional;

import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.restassured.http.ContentType;

import static com.raccoon.Constants.EMAIL_CLAIM;
import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@Testcontainers
@TestMethodOrder(OrderAnnotation.class)
@TestHTTPEndpoint(UserProfileResource.class)
@QuarkusTestResource(ElasticSearchTestResource.class)
@Transactional
class UserProfileResourceIT {

    static final String EXISTING_USERNAME = "the coon";

    @Inject
    MockMailbox mockMailbox;
    @Inject
    UserRepository userRepository;
    @Inject
    UserArtistRepository userArtistRepository;

    @BeforeEach
    @Transactional
    public void setup() {
        mockMailbox.clear();
        userArtistRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @TestSecurity(user = EXISTING_USERNAME, roles = "user")
    @OidcSecurity(claims = {
            @Claim(key = EMAIL_CLAIM, value = "user@gmail.com")
    })
    @DisplayName("successful get, should send welcome mail")
    void getProfileOnce() {
        given()
                .contentType(ContentType.JSON)
                .when().get()
                .then()
                .statusCode(SC_OK);

        assertEquals(1, mockMailbox.getMessagesSentTo("user@gmail.com").size());
    }

    @Test
    @TestSecurity(user = EXISTING_USERNAME, roles = "user")
    @OidcSecurity(claims = {
            @Claim(key = EMAIL_CLAIM, value = "user@gmail.com")
    })
    @DisplayName("successful get, called twice should send single welcome mail")
    void getProfileTwice() {
        // create the user
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

        assertEquals(1, mockMailbox.getMessagesSentTo("user@gmail.com").size());
    }

    @Test
    @TestSecurity(user = EXISTING_USERNAME, roles = "user")
    @OidcSecurity(claims = {
            @Claim(key = "email", value = "user@gmail.com")
    })
    @DisplayName("delete artist association")
    void unfollowArtist() {
        // create the user
        given()
                .contentType(ContentType.JSON)
                .when().get()
                .then()
                .statusCode(SC_OK);

        given()
                .contentType(ContentType.JSON)
                .when().post("/unfollow/1")
                .then()
                .statusCode(SC_OK);
    }

    @Test
    @TestSecurity(user = EXISTING_USERNAME, roles = "user")
    @OidcSecurity(claims = {
            @Claim(key = "email", value = "user@gmail.com")
    })
    @DisplayName("enableServices")
    void enableServices() {
        // create the user
        given()
                .contentType(ContentType.JSON)
                .when().get()
                .then()
                .statusCode(SC_OK);

        given()
                .contentType(ContentType.JSON)
                .param("lastfmUsername", "username")
                .param("enableSpotify", false)
                .when().get("/enableServices/")
                .then()
                .statusCode(SC_OK);
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