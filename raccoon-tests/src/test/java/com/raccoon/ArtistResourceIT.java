package com.raccoon;

import com.raccoon.artist.ArtistResource;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.repository.ArtistRepository;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.user.ArtistFollowingService;
import com.raccoon.user.dto.FollowedArtistDto;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;

import static com.raccoon.Constants.EMAIL_CLAIM;
import static io.restassured.RestAssured.given;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comments are not allowed in the `import-test.sql` file so clarifying here.
 */
@Slf4j
@QuarkusTest
@Testcontainers
@TestTransaction
@TestHTTPEndpoint(ArtistResource.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ArtistResourceIT {

    final static String EXISTING_USERNAME = "authenticated";

    @Inject
    ArtistRepository artistRepository;
    @Inject
    UserRepository userRepository;
    @Inject
    UserArtistRepository userArtistRepository;

    @Inject
    ArtistFollowingService artistFollowingService;

    @AfterEach
    @Transactional
    void tearDown() {
//        userArtistRepository.deleteAll();
//        artistRepository.deleteAll();
//        userRepository.deleteAll();
    }

    @Test
    @TestSecurity(user = EXISTING_USERNAME, roles = "user")
    @OidcSecurity(claims = {
            @Claim(key = EMAIL_CLAIM, value = "user100@mail.com")
    })
    void getRecommendedArtists_should_returnArtistsNotFollowedByUser() {
        List<FollowedArtistDto> artists = given()
                .contentType(ContentType.JSON)
                .param("page", "0")
                .param("size", "100")
                .when().get("/recommended")
                .then()
                .statusCode(SC_OK)
                .extract()
                .body().jsonPath().getList("rows", FollowedArtistDto.class);

        assertThat(artists)
                .hasSize(2)
                .extracting("name")
                .doesNotContain("existentArtist");
    }

    @Test
    @TestSecurity(user = EXISTING_USERNAME, roles = "user")
    @OidcSecurity(claims = {
            @Claim(key = EMAIL_CLAIM, value = "user100@mail.com")
    })
    void getRecommendedArtists_should_returnPaginatedArtistsNotFollowedByUser() {
        List<FollowedArtistDto> artists = given()
                .contentType(ContentType.JSON)
                .param("page", "0")
                .param("size", "1")
                .when().get("/recommended")
                .then()
                .statusCode(SC_OK)
                .extract()
                .body().jsonPath().getList("rows", FollowedArtistDto.class);

        assertThat(artists)
                .hasSize(1)
                .extracting("name")
                .containsAnyOf("existentArtist2", "existentArtist4");

        artists = given()
                .contentType(ContentType.JSON)
                .param("page", "1")
                .param("size", "1")
                .when().get("/recommended")
                .then()
                .statusCode(SC_OK)
                .extract()
                .body().jsonPath().getList("rows", FollowedArtistDto.class);

        assertThat(artists)
                .hasSize(1)
                .extracting("name")
                .containsAnyOf("existentArtist2", "existentArtist4");
    }

    @Test
    @TestSecurity(user = EXISTING_USERNAME, roles = "user")
    @OidcSecurity(claims = {
            @Claim(key = EMAIL_CLAIM, value = "user100@mail.com")
    })
    void getRecommendedArtists_should_returnArtistsNotFollowedByUser_when_userFollowsNewArtist() {
        List<FollowedArtistDto> recommendedArtists = given()
                .contentType(ContentType.JSON)
                .param("page", "0")
                .param("size", "100")
                .when().get("/recommended")
                .then()
                .statusCode(SC_OK)
                .extract()
                .body().jsonPath().getList("rows", FollowedArtistDto.class);
        FollowedArtistDto recommendedArtist = recommendedArtists.get(0);
        var artistToFollow = recommendedArtists.get(0);

        var userArtistRepoState = userArtistRepository.listAll().stream().toList();
        log.info("userartistrepository size: {}", userArtistRepoState.size());

        artistFollowingService.followArtist("user100@mail.com", artistRepository.findById(artistToFollow.getId()));

        userArtistRepoState = userArtistRepository.listAll().stream().toList();
        log.info("userartistrepository size: {} \n {}", userArtistRepoState.size(), userArtistRepoState.stream().map(UserArtist::toString).collect(Collectors.joining("\n")));

        List<FollowedArtistDto> artistsAfter = given()
                .contentType(ContentType.JSON)
                .param("page", "0")
                .param("size", "100")
                .when().get("/recommended")
                .then()
                .statusCode(SC_OK)
                .extract()
                .body().jsonPath().getList("rows", FollowedArtistDto.class);
        assertThat(artistsAfter)
                .hasSize(1)
                .extracting("name")
                .doesNotContain(artistToFollow.getName());
    }

}
