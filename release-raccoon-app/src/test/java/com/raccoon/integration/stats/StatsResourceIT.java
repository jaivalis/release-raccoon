package com.raccoon.integration.stats;

import com.raccoon.integration.profile.ArtistResourceDatabaseProfile;
import com.raccoon.stats.StatsResource;

import org.junit.jupiter.api.Test;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.is;

@Slf4j
@QuarkusTest
@TestHTTPEndpoint(StatsResource.class)
@TestProfile(value = ArtistResourceDatabaseProfile.class)
class StatsResourceIT {

    @Test
    @TestTransaction
    void getReleaseCount_should_return_correct_count_when_releases_exist() {
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
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/artists/count")
                .then()
                .statusCode(SC_OK)
                .body("count", is(2));
    }

    @Test
    @TestTransaction
    void getAllStats_should_return_both_counts_when_data_exists() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("")
                .then()
                .statusCode(SC_OK)
                .body("releaseCount", is(2))
                .body("artistCount", is(2));
    }

}