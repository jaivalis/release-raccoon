package com.raccoon.scraper.spotify;

import com.raccoon.entity.Artist;
import com.raccoon.entity.Release;
import com.raccoon.entity.factory.ArtistFactory;
import com.raccoon.entity.repository.ArtistReleaseRepository;
import com.raccoon.entity.repository.ArtistRepository;
import com.raccoon.entity.repository.ReleaseRepository;
import com.raccoon.scraper.ReleaseScraper;
import com.raccoon.scraper.TasteScraper;
import com.raccoon.scraper.mapper.SpotifyReleaseMapper;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.core5.http.ParseException;

import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Paging;

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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

/**
 * https://github.com/thelinmichael/spotify-web-api-java
 */
@Slf4j
@ApplicationScoped
public class SpotifyScraper implements ReleaseScraper<AlbumSimplified>, TasteScraper {

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

    public Set<AlbumSimplified> queryService(Optional<Integer> limit) throws InterruptedException {
        Set<AlbumSimplified> albums = new HashSet<>();

        var offset = 0;
        Paging<AlbumSimplified> response;
        try {
            do {
                response = spotifyApi.fetchNewReleasesPaginated(offset);
                AlbumSimplified[] pageItems = response.getItems();
                albums.addAll(Arrays.asList(pageItems));

                offset = response.getOffset() + response.getLimit();
            } while(response.getNext() != null && albums.size() < limit.orElse(Integer.MAX_VALUE));
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            log.error("Something went wrong when fetching new albums.", e);
            return albums;
        }
        return albums;
    }

    @Override
    @Transactional
    public Optional<Release> processRelease(AlbumSimplified albumSimplified) {
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

            return persistRelease(releaseArtists, release, releaseRepository, artistReleaseRepository);
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
        Paging<se.michaelthelin.spotify.model_objects.specification.Artist> response;
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

        artistRepository.persist(artists.stream().map(Pair::getKey));

        return artists;
    }

    public Artist processArtist(Object entry) {
        if (entry instanceof se.michaelthelin.spotify.model_objects.specification.Artist spotifyArtist) {
            log.info("Got spotify artist: {}", spotifyArtist.getName());
            var artist = artistFactory.getOrCreateArtist(spotifyArtist.getName());
            artist.setSpotifyUri(spotifyArtist.getUri());
            return artist;
        }
        throw new IllegalArgumentException("Got an object type that is not supported.");
    }

    // =========================================== End of TasteScraper API ========================================== //

}
