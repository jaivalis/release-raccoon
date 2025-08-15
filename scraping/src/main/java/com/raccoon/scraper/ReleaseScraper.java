package com.raccoon.scraper;

import com.raccoon.entity.Artist;
import com.raccoon.entity.ArtistRelease;
import com.raccoon.entity.Release;
import com.raccoon.entity.repository.ArtistReleaseRepository;
import com.raccoon.entity.repository.ReleaseRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

public interface ReleaseScraper<T> {

    Logger log = LoggerFactory.getLogger(ReleaseScraper.class);

    default Set<Release> scrapeReleases(Optional<Integer> limit) throws InterruptedException {
        return persistReleases(queryService(limit));
    }

    Set<T> queryService(Optional<Integer> limit) throws InterruptedException;

    @Transactional
    default Set<Release> persistReleases(Set<T> releases) {
        return releases.stream()
                .map(this::processRelease)
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());
    }

    Optional<Release> processRelease(T release);

    @Transactional
    default Optional<Release> persistRelease(Set<Artist> releaseArtists,
                                             Release release,
                                             ReleaseRepository releaseRepository,
                                             ArtistReleaseRepository artistReleaseRepository) {
        // Truncate name if it exceeds 300 characters
        if (release.getName() != null && release.getName().length() > 300) {
            log.warn("Truncating release name from {} to 300 characters: {}", 
                    release.getName().length(), release.getName());
            String truncated = release.getName().substring(0, 297) + "...";
            release.setName(truncated);
        }
        releaseRepository.persist(release);
        release.setReleases(
                releaseArtists
                        .stream()
                        .map(artist -> {
                            var artistRelease = new ArtistRelease();
                            artistRelease.setArtist(artist);
                            artistRelease.setRelease(release);

                            artistReleaseRepository.persist(artistRelease);
                            return artistRelease;
                        }).toList());
        releaseRepository.persist(release);
        return Optional.of(release);
    }

}
