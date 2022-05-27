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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        List<MusicbrainzReleasesResponse> pages;
        LocalDate today = LocalDate.now();
        pages = IntStream.range(0, 7)
                .mapToObj(i -> fetchReleasePagesForDay(today.minusDays(i)))
                .flatMap(Collection::stream)
                .toList();

        return pages;
    }

    private List<MusicbrainzReleasesResponse> fetchReleasePagesForDay(LocalDate today) {
        final List<MusicbrainzReleasesResponse> pages = new ArrayList<>();
        int offset = 0;
        MusicbrainzReleasesResponse responseForDate = null;
        do {
            try {
                responseForDate = client.searchReleasesByDate(today, offset);

                if (Objects.isNull(responseForDate.getError())) {
                    pages.add(responseForDate);
                } else {
                    log.error("Error while scraping Releases from Musicbrainz: {}", responseForDate.getError());
                }
                offset += 100;
            } catch (RuntimeException e) {
                log.error("Exception while scraping Musicbrainz releases", e);
            }
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
                .map(artistCredit -> {
                    final String releaseName = artistCredit.getArtist().getName();
                    Optional<Artist> byNameOptional = artistRepository.findByNameOptional(releaseName);

                    Artist artist = byNameOptional.isEmpty() ? new Artist() : byNameOptional.get();
                    if (byNameOptional.isEmpty()) {
                        artist.setName(releaseName);
                    }
                    if (artist.getMusicbrainzId() == null || artist.getMusicbrainzId().isEmpty()) {
                        artist.setMusicbrainzId(artistCredit.getArtist().getId());
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
