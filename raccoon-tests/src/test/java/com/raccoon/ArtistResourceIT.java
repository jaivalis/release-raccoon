package com.raccoon;

import com.raccoon.artist.ArtistResource;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.repository.ArtistRepository;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.user.ArtistFollowingService;
import com.raccoon.user.dto.FollowedArtistDto;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.TransactionManager;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.restassured.http.ContentType;
import lombok.SneakyThrows;
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
@TestHTTPEndpoint(ArtistResource.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ArtistResourceIT {

    final static String EXISTING_USERNAME = "authenticated";

    @Inject
    ArtistRepository artistRepository;
    @Inject
    UserArtistRepository userArtistRepository;

    @Inject
    ArtistFollowingService artistFollowingService;

    @Inject
    TransactionManager tm;

    @Test
    @TestSecurity(user = EXISTING_USERNAME, roles = "user")
    @OidcSecurity(claims = {
            @Claim(key = EMAIL_CLAIM, value = "user100@mail.com")
    })
    @Order(1)
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
    @Order(2)
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
    @SneakyThrows
    @Order(3)
    void getRecommendedArtists_should_returnArtistsNotFollowedByUser_when_userFollowsNewArtist() {
        tm.begin();
        List<FollowedArtistDto> recommendedArtists = given()
                .contentType(ContentType.JSON)
                .param("page", "0")
                .param("size", "100")
                .when().get("/recommended")
                .then()
                .statusCode(SC_OK)
                .extract()
                .body().jsonPath().getList("rows", FollowedArtistDto.class);
        var artistToFollow = recommendedArtists.get(0);

        var userArtistRepoState = userArtistRepository.listAll().stream().toList();
        log.info("userartistrepository size: {}", userArtistRepoState.size());

        // When a new artist is followed:
        Long artistToFollowId = artistToFollow.getId();
        artistFollowingService.followArtist("user100@mail.com", artistRepository.findById(artistToFollowId));
        tm.commit();

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
