package com.raccoon.scraper.spotify;

import com.raccoon.entity.Artist;
import com.raccoon.entity.ArtistRelease;
import com.raccoon.entity.Release;
import com.raccoon.scraper.ReleaseScraper;
import com.raccoon.scraper.TasteScraper;
import com.raccoon.scraper.config.SpotifyConfig;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.AlbumSimplified;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.validation.constraints.Max;

import lombok.extern.slf4j.Slf4j;

import static io.quarkus.hibernate.orm.panache.PanacheEntityBase.persist;

/**
 * https://github.com/thelinmichael/spotify-web-api-java
 */
@Slf4j
@ApplicationScoped
public class SpotifyScraper implements ReleaseScraper, TasteScraper {

    private final String clientId;
    private final String clientSecret;

    private SpotifyApi spotifyApi;

    private SpotifyUserAuth auth;

    private long credentialsExpiryTs = 0L;

    @Max(50)
    private static final int DEFAULT_LIMIT = 50;

    public SpotifyScraper(final SpotifyConfig config,
                          final SpotifyUserAuth auth) {
        clientId = config.getClientId();
        clientSecret = config.getClientSecret();
        this.auth = auth;
    }

    @PostConstruct
    private void init() {
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .build();
    }

    private void clientCredentials() throws InterruptedException {
        try {
            ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials()
                    .build();
            final CompletableFuture<ClientCredentials> clientCredentialsFuture = clientCredentialsRequest.executeAsync();

            final ClientCredentials clientCredentials = clientCredentialsFuture.get();
            credentialsExpiryTs = System.currentTimeMillis() + clientCredentials.getExpiresIn() * 1000;

            // Set access token for further "spotifyApi" object usage
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());

            log.info("Spotify client credentials expire in: {}", clientCredentials.getExpiresIn());
        } catch (CompletionException | CancellationException | ExecutionException e) {
            log.error("", e);
        } catch (InterruptedException e) {
            log.error("", e);
            throw e;
        }
    }

    // ============================================= ReleaseScraper API ============================================= //
    private Paging<AlbumSimplified> executeGetListOfNewReleasesRequest(final int offset)
            throws ParseException, SpotifyWebApiException, IOException, InterruptedException {
        if (System.currentTimeMillis() > credentialsExpiryTs) {
            clientCredentials();
        }
        return spotifyApi.getListOfNewReleases()
                .offset(offset)
                .limit(DEFAULT_LIMIT)
                .build().execute();
    }

    @Override
    public List<Release> scrapeReleases(Optional<Integer> limit) throws IOException, InterruptedException {
        List<Release> releases = new ArrayList<>();
        int offset = 0;
        Paging<AlbumSimplified> response;
        try {
            do {
                response = executeGetListOfNewReleasesRequest(offset);
                releases.addAll(
                        Arrays.stream(response.getItems())
                                .map(this::processRelease)
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .collect(Collectors.toList())
                );
                offset = response.getOffset() + response.getLimit();
            } while(response.getNext() != null);
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            log.error("Something went wrong when fetching new albums.\n", e);
            throw new IOException("Something went wrong when fetching new albums.", e);
        } catch (InterruptedException e) {
            log.error("", e);
            throw e;
        }
        return releases;
    }

    @Override
    public Optional<Release> processRelease(Object release) {
        if (release instanceof AlbumSimplified) {
            return processRelease((AlbumSimplified) release);
        }
        throw new IllegalArgumentException("Got an object type that is not supported.");
    }

    private Optional<Release> processRelease(AlbumSimplified albumSimplified) {
        log.debug("Processing release: {}", albumSimplified.getName());
        Set<Artist> releaseArtists = persistArtists(albumSimplified.getArtists());
        return persistRelease(albumSimplified, releaseArtists);
    }

    private Set<Artist> persistArtists(ArtistSimplified[] artists) {
        return Arrays.stream(artists)
                .distinct()
                .map(artistSimplified -> {
                    final String name = artistSimplified.getName();
                    Optional<Artist> byNameOptional = Artist.findByNameOptional(name);

                    Artist artist = byNameOptional.isEmpty() ? new Artist() : byNameOptional.get();
                    if (byNameOptional.isEmpty()) {
                        artist.setName(name);
                    }
                    if (artist.getSpotifyUri() == null || artist.getSpotifyUri().isEmpty()) {
                        artist.setSpotifyUri(artistSimplified.getUri());
                    }
                    persist(artist);
                    return artist;
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
    // ========================================== End of ReleaseScraper API ========================================= //
    // ============================================== TasteScraper API ============================================== //
    private Paging<com.wrapper.spotify.model_objects.specification.Artist> executeGetUsersTopArtists(final int offset)
            throws ParseException, SpotifyWebApiException, IOException, InterruptedException {
        if (System.currentTimeMillis() > credentialsExpiryTs) {
            clientCredentials();
        }
        return spotifyApi.getUsersTopArtists()
                .offset(offset)
                .limit(DEFAULT_LIMIT)
                .build().execute();
    }

    @Override
    public Collection<MutablePair<Artist, Float>> scrapeTaste(final String username,
                                                              final Optional<Integer> limit) {
        // This should be async. The SpotifyAuthResource should invoke the rest of the method once auth is complete for the user
        auth.authorizationCodeUri_Sync();
        List<MutablePair<Artist, Float>> releases = new ArrayList<>();
//        int offset = 0;
//        Paging<com.wrapper.spotify.model_objects.specification.Artist> response;
//        try {
//            do {
//                response = executeGetUsersTopArtists(offset);
//                releases.addAll(
//                        Arrays.stream(response.getItems())
//                                .map(artistObj ->
//                                        MutablePair.of(processArtist(artistObj), (float) 1) //(float) artistObj.getPlaycount())
//                                ).collect(Collectors.toList())
//                );
//                offset = response.getOffset() + response.getLimit();
//            } while(response.getNext() != null);
//        } catch (IOException | SpotifyWebApiException | ParseException e) {
//            log.error("Something went wrong when fetching new albums ", e);
////            throw new IOException("Something went wrong when fetching new albums.", e);
//        } catch (InterruptedException e) {
//            log.error("", e);
////            throw e;
//        }
        return releases;
    }

    @Override
    public Artist processArtist(Object entry) {
        return null;
    }
    // =========================================== End of TasteScraper API ========================================== //


}
