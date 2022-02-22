package com.raccoon.scraper.musicbrainz;

import com.raccoon.entity.Artist;
import com.raccoon.entity.Release;
import com.raccoon.entity.factory.ArtistFactory;
import com.raccoon.entity.repository.ArtistReleaseRepository;
import com.raccoon.entity.repository.ArtistRepository;
import com.raccoon.entity.repository.ReleaseRepository;
import com.raccoon.scraper.ReleaseScraper;
import com.raccoon.scraper.musicbrainz.dto.MusicbrainzReleasesResponse;
import com.raccoon.scraper.musicbrainz.dto.MusicbrainzReleasesResponse.MusicbrainzRelease;
import com.raccoon.scraper.musicbrainz.dto.mapper.MusicbrainzReleaseMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class MusicbrainzScraper implements ReleaseScraper {

    final MusicbrainzClient client;

    final ArtistFactory artistFactory;
    final ArtistRepository artistRepository;
    final ArtistReleaseRepository artistReleaseRepository;
    final ReleaseRepository releaseRepository;
    final MusicbrainzReleaseMapper releaseMapper;

    @Inject
    public MusicbrainzScraper(final ArtistFactory artistFactory,
                              final ArtistRepository artistRepository,
                              final ArtistReleaseRepository artistReleaseRepository,
                              final ReleaseRepository releaseRepository,
                              final MusicbrainzReleaseMapper releaseMapper,
                              final MusicbrainzClient client) {
        this.artistFactory = artistFactory;
        this.artistRepository = artistRepository;
        this.artistReleaseRepository = artistReleaseRepository;
        this.releaseRepository = releaseRepository;
        this.releaseMapper = releaseMapper;
        this.client = client;
    }

    @Override
    public Set<Release> scrapeReleases(Optional<Integer> limit) {
        List<MusicbrainzReleasesResponse> pages = fetchAllReleasePages();

        return pages.stream()
                .flatMap(response -> response.getReleases().stream())
                .map(this::processRelease)
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<Release> processRelease(Object release) {
        if (release instanceof MusicbrainzRelease album) {
            return processRelease(album);
        }
        throw new IllegalArgumentException("Got an object type that is not supported.");
    }

    private List<MusicbrainzReleasesResponse> fetchAllReleasePages() {
        List<MusicbrainzReleasesResponse> pages = new ArrayList<>();
        int offset = 0;
        MusicbrainzReleasesResponse responseForDate;

        LocalDate today = LocalDate.now();
        do {
            responseForDate = client.getForDate(today, offset);
            pages.add(responseForDate);
            offset += 100;
        } while (responseForDate != null && offset < responseForDate.getCount());

        return pages;
    }

    private Optional<Release> processRelease(MusicbrainzRelease musicbrainzRelease) {
        log.debug("Processing release: {}", musicbrainzRelease.getTitle());
        Set<Artist> releaseArtists = persistArtists(musicbrainzRelease.getArtistCredits());
        if (releaseArtists.isEmpty()) {
            log.warn("No artists found for release {}", musicbrainzRelease.getId());
            return Optional.empty();
        }
        return persistRelease(musicbrainzRelease, releaseArtists);
    }

    private Set<Artist> persistArtists(List<MusicbrainzReleasesResponse.ArtistCredit> artistCredits) {
        if (artistCredits == null || artistCredits.isEmpty()) {
            return Collections.emptySet();
        }

        return artistCredits.stream()
                .distinct()
                .map(musicbrainzRelease -> {
                    final String name = musicbrainzRelease.getName();
                    Optional<Artist> byNameOptional = artistRepository.findByNameOptional(name);

                    Artist artist = byNameOptional.isEmpty() ? new Artist() : byNameOptional.get();
                    if (byNameOptional.isEmpty()) {
                        artist.setName(name);
                    }
                    if (artist.getMusicbrainzId() == null || artist.getMusicbrainzId().isEmpty()) {
                        artist.setMusicbrainzId(musicbrainzRelease.getArtist().getId());
                    }
                    artistRepository.persist(artist);
                    return artist;
                }).collect(Collectors.toSet());
    }

    private Optional<Release> persistRelease(MusicbrainzRelease musicbrainzRelease, Set<Artist> releaseArtists) {
        Optional<Release> existing = releaseRepository.findMusicbrainzRelease(musicbrainzRelease.getId(), musicbrainzRelease.getTitle(), releaseArtists);

        if (existing.isEmpty()) {
            var releaseOptional = releaseMapper.fromAlbumSimplified(musicbrainzRelease);
            if (releaseOptional.isEmpty()) {
                return Optional.empty();
            }

            var release = releaseOptional.get();

            return persistRelease(releaseArtists, release, releaseRepository, artistReleaseRepository);
        } else {
            log.info("Release {} already in the database", musicbrainzRelease.getId());
            return Optional.empty();
        }
    }

}
