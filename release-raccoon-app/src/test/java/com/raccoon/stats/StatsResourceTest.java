package com.raccoon.stats;

import org.junit.jupiter.api.Test;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@QuarkusTest
class StatsResourceTest {

    @InjectMock
    StatsService statsService;

    @Test
    void getReleaseCount_should_return_count_when_called() {
        when(statsService.getReleaseCount()).thenReturn(42L);

        RestAssured.given()
                .when()
                .get("/stats/releases/count")
                .then()
                .statusCode(200)
                .body("count", is(42));
    }

    @Test
    void getArtistCount_should_return_count_when_called() {
        when(statsService.getArtistCount()).thenReturn(100L);

        RestAssured.given()
                .when()
                .get("/stats/artists/count")
                .then()
                .statusCode(200)
                .body("count", is(100));
    }

    @Test
    void getReleaseCount_should_return_zero_when_no_releases() {
        when(statsService.getReleaseCount()).thenReturn(0L);

        RestAssured.given()
                .when()
                .get("/stats/releases/count")
                .then()
                .statusCode(200)
                .body("count", is(0));
    }

    @Test
    void getArtistCount_should_return_zero_when_no_artists() {
        when(statsService.getArtistCount()).thenReturn(0L);

        RestAssured.given()
                .when()
                .get("/stats/artists/count")
                .then()
                .statusCode(200)
                .body("count", is(0));
    }

    @Test
    void getAllStats_should_return_both_counts_when_called() {
        when(statsService.getAllStats()).thenReturn(new StatsService.StatsResponse(50L, 25L));

        RestAssured.given()
                .when()
                .get("/stats")
                .then()
                .statusCode(200)
                .body("releaseCount", is(50))
                .body("artistCount", is(25));
    }

    @Test
    void getAllStats_should_return_zeros_when_no_data() {
        when(statsService.getAllStats()).thenReturn(new StatsService.StatsResponse(0L, 0L));

        RestAssured.given()
                .when()
                .get("/stats")
                .then()
                .statusCode(200)
                .body("releaseCount", is(0))
                .body("artistCount", is(0));
    }

    @Test
    void getAllStats_should_return_different_counts_when_different_data() {
        when(statsService.getAllStats()).thenReturn(new StatsService.StatsResponse(1000L, 250L));

        RestAssured.given()
                .when()
                .get("/stats")
                .then()
                .statusCode(200)
                .body("releaseCount", is(1000))
                .body("artistCount", is(250));
    }
}