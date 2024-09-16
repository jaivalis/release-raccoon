package com.raccoon.scraper.musicbrainz;

import com.raccoon.common.WiremockExtensions;
import com.raccoon.entity.Release;
import com.raccoon.scraper.musicbrainz.dto.MusicbrainzReleasesResponse;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@WithTestResource(WiremockExtensions.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MusicbrainzScraperTest {

    @Inject
    MusicbrainzScraper scraper;

    @Test
    @DisplayName("Uses the Wiremock stub provided in resources to verify correct deserialization")
    @TestTransaction
    void scrapeReleases() throws InterruptedException {
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
    @TestTransaction
    void processRelease() {
        MusicbrainzReleasesResponse.MusicbrainzRelease release = Instancio.create(MusicbrainzReleasesResponse.MusicbrainzRelease.class);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        release.setDate(formatter.format(LocalDate.now()));

        assertThat(scraper.processRelease(release))
                .isPresent();
    }

    @Test
    @TestTransaction
    void processFutureRelease() {
        MusicbrainzReleasesResponse.MusicbrainzRelease release = Instancio.create(MusicbrainzReleasesResponse.MusicbrainzRelease.class);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        release.setDate(formatter.format(LocalDate.now().plusDays(7)));

        assertThat(scraper.processRelease(release)).isEmpty();
    }

    @Test
    @TestTransaction
    void processReleaseBadDate() {
        MusicbrainzReleasesResponse.MusicbrainzRelease release = Instancio.create(MusicbrainzReleasesResponse.MusicbrainzRelease.class);
        release.setDate("2024");

        assertThat(scraper.processRelease(release)).isEmpty();
    }

    @Test
    @TestTransaction
    void processReleaseNullDate() {
        MusicbrainzReleasesResponse.MusicbrainzRelease release = Instancio.create(MusicbrainzReleasesResponse.MusicbrainzRelease.class);
        release.setDate(null);

        assertThat(scraper.processRelease(release)).isEmpty();
    }

    @Test
    @TestTransaction
    void processReleaseNoArtists() {
        MusicbrainzReleasesResponse.MusicbrainzRelease release = Instancio.create(MusicbrainzReleasesResponse.MusicbrainzRelease.class);
        release.setArtistCredits(null);

        assertThat(scraper.processRelease(release)).isEmpty();
    }

}