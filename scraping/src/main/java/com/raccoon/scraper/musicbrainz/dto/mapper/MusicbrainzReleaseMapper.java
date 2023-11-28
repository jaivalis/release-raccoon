package com.raccoon.scraper.musicbrainz.dto.mapper;

import com.raccoon.entity.Release;
import com.raccoon.scraper.musicbrainz.dto.MusicbrainzReleasesResponse;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

/**
 * Parses a MusicbrainzReleasesResponse.MusicbrainzRelease to Release entity.
 *
 * Musicbrainz also returns planned (future) releases. Those are of no interest of now and are
 * discarded.
 */
@Slf4j
@ApplicationScoped
public class MusicbrainzReleaseMapper {

    /**
     * Projects a dto onto a Release Entity if the MusicbrainzRelease has already been released.
     * @param musicbrainzRelease the dto as obtained from MusicbrainzRelease
     * @return Optional of the new release, or empty if the release has not been released (determined by release date)
     */
    public Optional<Release> fromAlbumSimplified(MusicbrainzReleasesResponse.MusicbrainzRelease musicbrainzRelease) {
        Optional<LocalDate> releaseDate = parsePastReleaseDate(musicbrainzRelease.getDate());
        if (releaseDate.isEmpty()) {
            return Optional.empty();
        }

        final var release = new Release();

        release.setName(musicbrainzRelease.getTitle());
        release.setReleasedOn(releaseDate.get());
        release.setMusicbrainzId(musicbrainzRelease.getId());

        releaseType(musicbrainzRelease).ifPresent(release::setType);

        return Optional.of(release);
    }

    /**
     * Returns the release date if it was in the past, otherwise empty
     * @param dateString the string coming from MusicbrainzRelease
     * @return Optional<LocalDate> if it was in the past, otherwise empty
     */
    Optional<LocalDate> parsePastReleaseDate(String dateString) {
        if (dateString == null) {
            return Optional.empty();
        }
        try {
            LocalDate parsed = LocalDate.parse(dateString);
            if (parsed.isAfter(LocalDate.now())) {
                // this is a planned release, has not been made yet, no notifications necessary
                return Optional.empty();
            }
            return Optional.of(parsed);
        } catch (DateTimeParseException e) {
            log.warn("Exception occurred when parsing release date {}", dateString);
            return Optional.empty();
        }
    }

    Optional<String> releaseType(MusicbrainzReleasesResponse.MusicbrainzRelease musicbrainzRelease) {
        if (musicbrainzRelease.getReleaseGroup() != null) {
            return Optional.ofNullable(musicbrainzRelease.getReleaseGroup().getPrimaryType());
        }
        return Optional.empty();
    }

}
