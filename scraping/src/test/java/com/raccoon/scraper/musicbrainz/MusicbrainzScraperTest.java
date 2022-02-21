package com.raccoon.scraper.musicbrainz;

import com.flextrade.jfixture.JFixture;
import com.raccoon.common.ElasticSearchTestResource;
import com.raccoon.common.WiremockExtensions;
import com.raccoon.entity.Release;
import com.raccoon.scraper.musicbrainz.dto.MusicbrainzReleasesResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
@QuarkusTestResource(ElasticSearchTestResource.class)
@QuarkusTestResource(H2DatabaseTestResource.class)
@QuarkusTestResource(WiremockExtensions.class)
class MusicbrainzScraperTest {

    @Inject
    MusicbrainzScraper scraper;

    @Test
    @DisplayName("Uses the Wiremock stub provided in resources to verify correct deserialization")
    @TestTransaction
    void scrapeReleases() {
        Set<Release> releases = scraper.scrapeReleases(Optional.empty());

        assertThat(releases)
                .as("Should contain only released items (no future releases)")
                .hasSize(22)
                .doesNotHaveDuplicates();
        assertThat(releases).extracting("name")
                .as("Should not contain a release for which the release date is not ISO_FORMAT")
                .doesNotContain("Eꜱᴘᴇʀᴡᴀᴠᴇ ≈ エスパーウェーブ")
                .as("Should contain releases for which the release date is not ISO_FORMAT")
                .contains("Lordiversity - Humanimals", "W", "Symphonies nos. 1 & 3");
    }

    @Test
    void processReleaseIllegal() {
        assertThrows(IllegalArgumentException.class, () -> scraper.processRelease(2));
    }

    @Test
    @TestTransaction
    void processRelease() {
        JFixture fixture = new JFixture();
        MusicbrainzReleasesResponse.MusicbrainzRelease release = fixture.create(MusicbrainzReleasesResponse.MusicbrainzRelease.class);

        DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        release.setDate(DATE_TIME_FORMATTER.format(LocalDate.now()));

        assertThat(scraper.processRelease(release))
                .isPresent();
    }

    @Test
    @TestTransaction
    void processFutureRelease() {
        JFixture fixture = new JFixture();
        MusicbrainzReleasesResponse.MusicbrainzRelease release = fixture.create(MusicbrainzReleasesResponse.MusicbrainzRelease.class);
        DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        release.setDate(DATE_TIME_FORMATTER.format(LocalDate.now().plusDays(7)));

        assertThat(scraper.processRelease(release)).isEmpty();
    }

    @Test
    @TestTransaction
    void processReleaseBadDate() {
        JFixture fixture = new JFixture();
        MusicbrainzReleasesResponse.MusicbrainzRelease release = fixture.create(MusicbrainzReleasesResponse.MusicbrainzRelease.class);
        release.setDate("2024");

        assertThat(scraper.processRelease(release)).isEmpty();
    }

    @Test
    @TestTransaction
    void processReleaseNullDate() {
        JFixture fixture = new JFixture();
        MusicbrainzReleasesResponse.MusicbrainzRelease release = fixture.create(MusicbrainzReleasesResponse.MusicbrainzRelease.class);
        release.setDate(null);

        assertThat(scraper.processRelease(release)).isEmpty();
    }

}