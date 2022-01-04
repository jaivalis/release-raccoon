package com.raccoon;

import com.github.database.rider.cdi.api.DBRider;
import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.raccoon.common.ElasticSearchTestResource;
import com.raccoon.entity.Release;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.release.ReleaseScrapeResource;
import com.raccoon.scraper.spotify.SpotifyScraper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

import static io.restassured.RestAssured.given;
import static java.util.Collections.emptyList;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;


@QuarkusTest
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestHTTPEndpoint(ReleaseScrapeResource.class)
@DBRider
@DBUnit(caseSensitiveTableNames = true)
@QuarkusTestResource(ElasticSearchTestResource.class)
class ReleaseScrapeResourceIT {

    @Inject
    UserArtistRepository userArtistRepository;
    @InjectMock
    SpotifyScraper mockScraper;

    @BeforeEach
    public void setup() {
        QuarkusMock.installMockForType(mockScraper, SpotifyScraper.class);
    }

    @Test
    @Order(1)
    @DataSet(value = "datasets/yml/release-scrape.yml")
    @DisplayName("new releases should set UserArtist.hasNewRelease")
    void releaseScrape_should_mark_hasNewRelease() throws IOException, InterruptedException {
        Release scrapedRelease = Release.findById(100L);
        List<Release> mockReleases = List.of(
                scrapedRelease
        );
        Mockito.when(mockScraper.scrapeReleases(any())).thenReturn(mockReleases);

        final ValidatableResponse response = given()
                .when().get()
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(containsString(scrapedRelease.getName()));
        var byUser = userArtistRepository.findByUserId(100L);
        assertEquals(1, byUser.size());
        assertTrue((byUser.get(0)).getHasNewRelease());
    }

    @Test
    @Order(2)
    @DataSet(value = "datasets/yml/release-scrape.yml")
    @DisplayName("no new releases should return empty list")
    void releaseScrape_emptyList() throws IOException, InterruptedException {
        Mockito.when(mockScraper.scrapeReleases(any())).thenReturn(emptyList());

        Release[] result = given()
                .when().get()
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .as(Release[].class);
        assertEquals(0, result.length);
    }

}
