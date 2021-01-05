package com.raccoon.scraper;


import com.raccoon.config.SpotifyConfig;
import com.raccoon.entity.Artist;
import com.raccoon.entity.ArtistRelease;
import com.raccoon.entity.Release;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.AlbumSimplified;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.hc.core5.http.ParseException;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static io.quarkus.hibernate.orm.panache.PanacheEntityBase.persist;

/**
 * https://github.com/thelinmichael/spotify-web-api-java
 */
@Slf4j
@ApplicationScoped
public class SpotifyScraper implements ReleaseScraper {

    private final String clientId;
    private final String clientSecret;

    private static SpotifyApi spotifyApi;

    private static ClientCredentialsRequest clientCredentialsRequest;

    private static final int DEFAULT_LIMIT = 10;

    public SpotifyScraper(SpotifyConfig config) {
        clientId = config.getClientId();
        clientSecret = config.getClientSecret();
    }

    @PostConstruct
    private void init() throws InterruptedException {
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .build();
        clientCredentials();
    }

    private static void clientCredentials() throws InterruptedException {
        try {
            clientCredentialsRequest = spotifyApi.clientCredentials()
                    .build();
            final CompletableFuture<ClientCredentials> clientCredentialsFuture = clientCredentialsRequest.executeAsync();

            final ClientCredentials clientCredentials = clientCredentialsFuture.get();

            // Set access token for further "spotifyApi" object usage
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());

            log.info("Spotify client credentials expire in: " + clientCredentials.getExpiresIn());
        } catch (CompletionException | CancellationException | ExecutionException e) {
            log.error("", e);
        } catch (InterruptedException e) {
            log.error("", e);
            throw e;
        }
    }

    @Override
    public void scrapeReleases(Optional<Integer> limit) {
        try {
            final Paging<AlbumSimplified> albumSimplifiedPaging =
                    spotifyApi.getListOfNewReleases()
                            .limit(limit.orElse(DEFAULT_LIMIT))
                            .build().execute();
            for (val release : albumSimplifiedPaging.getItems()) {
                processRelease(release);
            }
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            log.error("Something went wrong when fetching new albums.\n", e);
        }
    }

    @Override
    public void processRelease(Object release) {
        if (release instanceof AlbumSimplified) {
            processRelease((AlbumSimplified) release);
        }
    }

    private void processRelease(AlbumSimplified albumSimplified) {
        log.debug("Processing release: {}", albumSimplified.getName());
        Set<Artist> releaseArtists = persistArtists(albumSimplified.getArtists());
        persistRelease(albumSimplified, releaseArtists);
    }

    private Set<Artist> persistArtists(ArtistSimplified[] artists) {
        return Arrays.stream(artists)
                .distinct()
                .map(artistSimplified -> {
                    final String name = artistSimplified.getName();
                    Optional<Artist> byNameOptional = Artist.findByNameOptional(name);
                    if (byNameOptional.isEmpty()) {
                        Artist artist = new Artist();
                        artist.setName(name);
                        artist.setSpotifyUri(artistSimplified.getUri());

                        persist(artist);
                        return artist;
                    }
                    return byNameOptional.get();
                }).collect(Collectors.toSet());
    }

    private Optional<Release> persistRelease(AlbumSimplified albumSimplified, Set<Artist> releaseArtists) {
        if (Release.findBySpotifyUriOptional(albumSimplified.getUri()).isEmpty()) {
            final Release release = new Release();
            release.setName(albumSimplified.getName());
            release.setType(albumSimplified.getAlbumType().toString());
            release.setSpotifyUri(albumSimplified.getUri());
            release.setReleasedOn(LocalDate.parse(albumSimplified.getReleaseDate()));

            persist(release);  // do I need this many persists?

            release.setReleases(releaseArtists
                    .stream()
                    .map(artist -> {
                        ArtistRelease artistRelease = new ArtistRelease();
                        artistRelease.setArtist(artist);
                        artistRelease.setRelease(release);

                        persist(artistRelease);
                        return artistRelease;
                    }).collect(Collectors.toList()));
            persist(release);
            return Optional.of(release);
        }
        return Optional.empty();
    }


}
