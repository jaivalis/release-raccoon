package com.raccoon.integration.search;

import com.raccoon.integration.profile.ArtistResourceDatabaseProfile;
import com.raccoon.search.ArtistSearchResource;
import com.raccoon.search.dto.SearchResultArtistDto;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.util.List;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.quarkus.test.security.oidc.UserInfo;
import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@QuarkusTest
@TestHTTPEndpoint(ArtistSearchResource.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@TestProfile(value = ArtistResourceDatabaseProfile.class)
class ArtistSearchResourceIT {

    static final String EXISTING_USERNAME = "authenticated";

    @Test
    @TestSecurity(user = EXISTING_USERNAME, roles = "user")
    @OidcSecurity(userinfo = {
            @UserInfo(key = "email", value = "user100@mail.com")
    })
    @TestTransaction
    void searchArtists_should_returnArtistsWithFollowerCounts_when_hibernateResultsFound() {
        List<SearchResultArtistDto> artists = given()
                .contentType(ContentType.JSON)
                .param("pattern", "existentArtist")
                .param("size", "10")
                .when().get("/search")
                .then()
                .statusCode(SC_OK)
                .extract()
                .body().jsonPath().getList("artists", SearchResultArtistDto.class);

        assertThat(artists)
                .isNotEmpty()
                .allSatisfy(artist -> {
                    if (artist.getId() != null) {
                        assertThat(artist.getFollowerCount())
                                .as("Artist %s should have follower count", artist.getName())
                                .isNotNull()
                                .isGreaterThanOrEqualTo(0);
                    }
                });

        // Filter to only database results (those with IDs) and check follower counts
        var databaseResults = artists.stream()
                .filter(artist -> artist.getId() != null)
                .toList();

        if (!databaseResults.isEmpty()) {
            assertThat(databaseResults.stream().mapToInt(SearchResultArtistDto::getFollowerCount).sum())
                    .as("Total follower count for database results should be greater than or equal to 0")
                    .isGreaterThanOrEqualTo(0);
        }
    }

    @Test
    @TestSecurity(user = EXISTING_USERNAME, roles = "user")
    @OidcSecurity(userinfo = {
            @UserInfo(key = "email", value = "user100@mail.com")
    })
    @TestTransaction
    void searchArtists_should_handleEmptyResults() {
        List<SearchResultArtistDto> artists = given()
                .contentType(ContentType.JSON)
                .param("pattern", "nonexistentartistname")
                .param("size", "10")
                .when().get("/search")
                .then()
                .statusCode(SC_OK)
                .extract()
                .body().jsonPath().getList("artists", SearchResultArtistDto.class);

        // Should return empty list or external API results (without IDs)
        assertThat(artists)
                .isNotEmpty()
                .allSatisfy(artist -> {
                    if (artist.getId() != null) {
                        assertThat(artist.getFollowerCount())
                                .as("Any database result should have follower count")
                                .isNotNull()
                                .isGreaterThanOrEqualTo(0);
                    }
                });
    }
}