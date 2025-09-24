package com.raccoon.integration.resource;

import com.raccoon.integration.profile.ReleaseScrapeDatabaseProfile;
import com.raccoon.user.UserProfileResource;
import com.raccoon.user.dto.FollowedArtistsRelease;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.quarkus.test.security.oidc.UserInfo;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestHTTPEndpoint(UserProfileResource.class)
@WithTestResource(H2DatabaseTestResource.class)
@TestTransaction
@TestProfile(value = ReleaseScrapeDatabaseProfile.class)
class RaccoonUserProfileRecommendedResourceIT {

    static final String EXISTING_USERNAME = "the coon";

    @Test
    @TestTransaction
    @TestSecurity(user = EXISTING_USERNAME, roles = "user")
    @OidcSecurity(userinfo = {
            @UserInfo(key = "email", value = "user100@mail.com")
    })
    @DisplayName("GET `/me/followed-artists/releases` with days parameter")
    void getFollowedArtistsReleases_should_returnReleases() {
        var response = given()
                .contentType(ContentType.JSON)
                .queryParam("days", 7)
                .when().get("followed-artists/releases")
                .then()
                .statusCode(SC_OK)
                .extract().body().jsonPath();

        List<FollowedArtistsRelease> allReleases = response.getList("releases", FollowedArtistsRelease.class);
        Integer total = response.getInt("total");

        assertThat(total).isEqualTo(1);
        assertThat(allReleases.getFirst())
                .extracting("id", "name", "type")
                .containsOnly(100L, "newRelease", "ALBUM");
    }

    @Test
    @TestTransaction
    @TestSecurity(user = EXISTING_USERNAME, roles = "user")
    @OidcSecurity(userinfo = {
            @UserInfo(key = "email", value = "user100@mail.com")
    })
    @DisplayName("GET `/me/followed-artists/releases` with days parameter")
    void getFollowedArtistsReleases_should_filterOutReleases() {
        var response = given()
                .contentType(ContentType.JSON)
                .queryParam("days", 1)
                .when().get("followed-artists/releases")
                .then()
                .statusCode(SC_OK)
                .extract().body().jsonPath();

        List<FollowedArtistsRelease> allReleases = response.getList("releases", FollowedArtistsRelease.class);
        Integer total = response.getInt("total");

        assertThat(total).isZero();
        assertThat(allReleases).isEmpty();
    }

}
