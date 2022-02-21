package com.raccoon.scraper.spotify;

import com.raccoon.entity.Artist;
import com.raccoon.entity.ArtistRelease;
import com.raccoon.entity.Release;
import com.raccoon.entity.factory.ArtistFactory;
import com.raccoon.entity.repository.ArtistReleaseRepository;
import com.raccoon.entity.repository.ArtistRepository;
import com.raccoon.entity.repository.ReleaseRepository;
import com.raccoon.scraper.ReleaseScraper;
import com.raccoon.scraper.TasteScraper;
import com.raccoon.scraper.mapper.SpotifyReleaseMapper;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.AlbumSimplified;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Paging;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

/**
 * https://github.com/thelinmichael/spotify-web-api-java
 */
@Slf4j
@ApplicationScoped
public class SpotifyScraper implements ReleaseScraper, TasteScraper {

    final ArtistFactory artistFactory;
    final ArtistRepository artistRepository;
    final ArtistReleaseRepository artistReleaseRepository;
    final ReleaseRepository releaseRepository;
    final SpotifyReleaseMapper releaseMapper;

    final RaccoonSpotifyApi spotifyApi;

    @Inject
    public SpotifyScraper(final ArtistFactory artistFactory,
                          final ArtistRepository artistRepository,
                          final ArtistReleaseRepository artistReleaseRepository,
                          final ReleaseRepository releaseRepository,
                          final SpotifyReleaseMapper releaseMapper,
                          final RaccoonSpotifyApi spotifyApi) {
        this.artistFactory = artistFactory;
        this.artistRepository = artistRepository;
        this.artistReleaseRepository = artistReleaseRepository;
        this.releaseRepository = releaseRepository;
        this.releaseMapper = releaseMapper;
        this.spotifyApi = spotifyApi;
    }

    // ============================================= ReleaseScraper API ============================================= //

    @Override
    public Set<Release> scrapeReleases(Optional<Integer> limit) throws InterruptedException {
        Set<Release> releases = new HashSet<>();
        var offset = 0;
        Paging<AlbumSimplified> response;
        try {
            do {
                response = spotifyApi.fetchNewReleasesPaginated(offset);
                releases.addAll(
                        Arrays.stream(response.getItems())
                                .map(this::processRelease)
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .toList()
                );
                offset = response.getOffset() + response.getLimit();
            } while(response.getNext() != null);
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            log.error("Something went wrong when fetching new albums.", e);
            return Collections.emptySet();
        } catch (InterruptedException e) {
            throw e;
        }
        return releases;
    }

    @Override
    public Optional<Release> processRelease(Object release) {
        if (release instanceof AlbumSimplified album) {
            return processRelease(album);
        }
        throw new IllegalArgumentException("Got an object type that is not supported.");
    }

    private Optional<Release> processRelease(AlbumSimplified albumSimplified) {
        log.debug("Processing release: {}", albumSimplified.getName());
        Set<Artist> releaseArtists = persistArtists(albumSimplified.getArtists());
        if (releaseArtists.isEmpty()) {
            return Optional.empty();
        }
        return persistRelease(albumSimplified, releaseArtists);
    }

    private Set<Artist> persistArtists(ArtistSimplified[] artists) {
        if (artists == null || artists.length == 0) {
            return Collections.emptySet();
        }

        return Arrays.stream(artists)
                .distinct()
                .map(artistSimplified -> {
                    final String name = artistSimplified.getName();
                    Optional<Artist> byNameOptional = artistRepository.findByNameOptional(name);

                    Artist artist = byNameOptional.isEmpty() ? new Artist() : byNameOptional.get();
                    if (byNameOptional.isEmpty()) {
                        artist.setName(name);
                    }
                    if (artist.getSpotifyUri() == null || artist.getSpotifyUri().isEmpty()) {
                        artist.setSpotifyUri(artistSimplified.getUri());
                    }
                    artistRepository.persist(artist);
                    return artist;
                }).collect(Collectors.toSet());
    }

    private Optional<Release> persistRelease(AlbumSimplified albumSimplified, Set<Artist> releaseArtists) {
        Optional<Release> existing = releaseRepository.findSpotifyRelease(albumSimplified.getUri(), albumSimplified.getName(), releaseArtists);

        if (existing.isEmpty()) {
            final var release = releaseMapper.fromAlbumSimplified(albumSimplified);

            releaseRepository.persist(release);  // do I need this many persists?

            release.setReleases(releaseArtists
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
        return Optional.empty();
    }

    // ========================================== End of ReleaseScraper API ========================================= //
    // ============================================== TasteScraper API ============================================== //

    @Override
    public Collection<MutablePair<Artist, Float>> scrapeTaste(String username, Optional<Integer> limit) {
        throw new UnsupportedOperationException("Invoked asynchronously from the Spotify OAuth cycle instead.");
    }

    public Collection<MutablePair<Artist, Float>> fetchTopArtists(final SpotifyUserAuthorizer authorizer) {
        List<MutablePair<Artist, Float>> artists = new ArrayList<>();
        var offset = 0;
        Paging<com.wrapper.spotify.model_objects.specification.Artist> response;
        try {
            do {
                response = authorizer.executeGetUsersTopArtists(offset);
                artists.addAll(
                        Arrays.stream(response.getItems())
                                .map(artistObj ->
                                        MutablePair.of(processArtist(artistObj), (float) 1) //(float) artistObj.getPlaycount())
                                ).toList()
                );
                offset = response.getOffset() + response.getLimit();
            } while(response.getNext() != null);
        } catch (IOException | ParseException | SpotifyWebApiException e) {
            log.error("Something went wrong when fetching Spotify artists ", e);
        }
        return artists;
    }

    public Artist processArtist(Object entry) {
        if (entry instanceof com.wrapper.spotify.model_objects.specification.Artist spotifyArtist) {
            log.info("Got spotify artist: {}", spotifyArtist.getName());
            var artist = artistFactory.getOrCreateArtist(spotifyArtist.getName());
            artist.setSpotifyUri(spotifyArtist.getUri());
            artistRepository.persist(artist);

            return artist;
        }
        throw new IllegalArgumentException("Got an object type that is not supported.");
    }
    // =========================================== End of TasteScraper API ========================================== //

}
