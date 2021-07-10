package com.raccoon;

import com.raccoon.entity.Artist;
import com.raccoon.registration.UserRegisteringResource;
import com.raccoon.scraper.LastfmScraper;

import org.apache.commons.lang3.tuple.MutablePair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Collection;
import java.util.Collections;

import javax.transaction.Transactional;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_CONFLICT;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@QuarkusTest
@Testcontainers
@TestMethodOrder(OrderAnnotation.class)
@TestHTTPEndpoint(UserRegisteringResource.class)
@Transactional
class UserRegisteringResourceTest {

    final String EXISTING_USERNAME = "the coon";

    @InjectMock
    LastfmScraper mock;

    @BeforeEach
    public void setup() {
        QuarkusMock.installMockForType(mock, LastfmScraper.class);
    }

    @Test
    @Order(1)
    @TestSecurity(user = EXISTING_USERNAME, roles = "user")
    @OidcSecurity(claims = {
            @Claim(key = "email", value = "user@gmail.com")
    })
    @DisplayName("successful registration")
    void successfulRegistration() {
        Collection<MutablePair<Artist, Float>> mockTaste = Collections.emptyList();
        Mockito.when(mock.scrapeTaste(eq(EXISTING_USERNAME), any())).thenReturn(mockTaste);

        given()
                .contentType(ContentType.JSON)
                .when().get()
                .then()
                .statusCode(SC_OK);
    }

    @Test
    @Order(2)
    @TestSecurity(user = EXISTING_USERNAME, roles = "user")
    @OidcSecurity(claims = {
            @Claim(key = "email", value = "user@gmail.com")
    })
    @DisplayName("existing email should yield 409")
    void existingEmail() {
        Collection<MutablePair<Artist, Float>> mockTaste = Collections.emptyList();
        Mockito.when(mock.scrapeTaste(eq(EXISTING_USERNAME), any())).thenReturn(mockTaste);

        given()
                .contentType(ContentType.JSON)
                .when().get()
                .then()
                .statusCode(SC_CONFLICT);
    }

    @Test
    @DisplayName("no bearer toke, unauthorized")
    void unauthorized() {
        given()
                .contentType(ContentType.JSON)
                .when().get()
                .then()
                .statusCode(SC_UNAUTHORIZED);
    }

}