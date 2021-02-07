package com.raccoon;

import com.raccoon.entity.Artist;
import com.raccoon.scraper.LastfmScraper;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.tuple.MutablePair;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Collections;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_CONFLICT;
import static org.apache.http.HttpStatus.SC_OK;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@Testcontainers
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
class UserRegisteringResourceTest {

    @InjectMock
    LastfmScraper mock;

    @BeforeEach
    public void setup() {
        QuarkusMock.installMockForType(mock, LastfmScraper.class);
    }

    final String EXISTING_USERNAME = "username";

    @Test
    @Order(1)
    void test_register_empty_results() {
        Collection<MutablePair<Artist, Float>> mockTaste = Collections.emptyList();
        Mockito.when(mock.scrapeTaste(eq(EXISTING_USERNAME), any())).thenReturn(mockTaste);

        String body = new JsonObject()
                .put("email", "someone@email.com")
                .put("lastfmUsername", EXISTING_USERNAME)
                .toString();

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/register")
                .then()
                .statusCode(SC_OK);
    }

    @Test
    @Order(2)
    void test_register_existing_email() {
        Collection<MutablePair<Artist, Float>> mockTaste = Collections.emptyList();
        Mockito.when(mock.scrapeTaste(eq(EXISTING_USERNAME), any())).thenReturn(mockTaste);

        String body = new JsonObject()
                .put("email", "someone@email.com")
                .put("lastfmUsername", EXISTING_USERNAME)
                .toString();

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/register")
                .then()
                .statusCode(SC_CONFLICT);
    }

}