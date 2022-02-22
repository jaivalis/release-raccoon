package com.raccoon.scraper;

import com.raccoon.entity.Artist;
import com.raccoon.entity.ArtistRelease;
import com.raccoon.entity.Release;
import com.raccoon.entity.repository.ArtistReleaseRepository;
import com.raccoon.entity.repository.ReleaseRepository;

import java.util.Optional;
import java.util.Set;

public interface ReleaseScraper {

    Set<Release> scrapeReleases(Optional<Integer> limit) throws InterruptedException;

    Optional<Release> processRelease(Object release);

    default Optional<Release> persistRelease(Set<Artist> releaseArtists,
                                             Release release,
                                             ReleaseRepository releaseRepository,
                                             ArtistReleaseRepository artistReleaseRepository) {
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
