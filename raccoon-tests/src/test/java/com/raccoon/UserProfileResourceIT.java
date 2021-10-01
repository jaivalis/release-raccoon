package com.raccoon;

import com.raccoon.registration.RegisteringService;
import com.raccoon.user.UserProfileResource;
import com.raccoon.user.UserProfileService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.inject.Inject;
import javax.transaction.Transactional;

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

@QuarkusTest
@Testcontainers
@TestMethodOrder(OrderAnnotation.class)
@TestHTTPEndpoint(UserProfileResource.class)
@Transactional
class UserProfileResourceIT {

    static final String EXISTING_USERNAME = "the coon";

    @Inject
    UserProfileService service;

    @Inject
    UserProfileService userProfileService;
    @Inject
    RegisteringService registeringService;

    @BeforeEach
    public void setup() {
    }

    @Test
    @TestSecurity(user = EXISTING_USERNAME, roles = "user")
    @OidcSecurity(claims = {
            @Claim(key = EMAIL_CLAIM, value = "user@gmail.com")
    })
    @DisplayName("successful registerCallback")
    void registerCallback() {
        given()
                .contentType(ContentType.JSON)
                .when().get()
                .then()
                .statusCode(SC_OK);
    }

    @Test
    @TestSecurity(user = EXISTING_USERNAME, roles = "user")
    @OidcSecurity(claims = {
            @Claim(key = "email", value = "user@gmail.com")
    })
    @DisplayName("delete artist association")
    void unfollowArtist() {
        given()
                .contentType(ContentType.JSON)
                .when().post("/unfollow/1")
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