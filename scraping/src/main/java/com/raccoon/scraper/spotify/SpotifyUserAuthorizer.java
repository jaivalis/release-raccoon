package com.raccoon.scraper.spotify;

import com.raccoon.scraper.config.SpotifyConfig;

import org.apache.hc.core5.http.ParseException;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.Paging;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

/**
 * More documentation on the auth flow:
 * https://developer.spotify.com/documentation/general/guides/authorization-guide/
 *
 * Relevant code snippets:
 * https://github.com/thelinmichael/spotify-web-api-java/blob/master/examples/authorization/authorization_code/AuthorizationCodeUriExample.java
 */
@Slf4j
@ApplicationScoped
public class SpotifyUserAuthorizer {

    private final String clientId;
    private final String clientSecret;
    private final URI authCallbackUri;

    SpotifyApi spotifyApi;

    public SpotifyUserAuthorizer(SpotifyConfig config) {
        clientId = config.clientId();
        clientSecret = config.clientSecret();
        authCallbackUri = URI.create(config.authCallbackUri());
    }

    @PostConstruct
    void init() {
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRedirectUri(authCallbackUri)
                .build();
    }

    // ============================== Spotify util code ==============================

    /**
     *
     * @param userId used to propagate the userId on consecutive auth calls
     * @return
     */
    public URI authorizationCodeUriSync(String userId) {
        var authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
                .state(userId)
                .scope("user-top-read")
                .redirect_uri(authCallbackUri)
                .show_dialog(true)
                .build();
        final var uri = authorizationCodeUriRequest.execute();

        log.info("Spotify auth URI: {}", uri.toString());

        return uri;
    }

    public void authorizationCodeUriAsync(final String email) {
        try {
            var authorizationCodeUriRequest =
                    spotifyApi.authorizationCodeUri()
                            .state(email)
                            .scope("user-top-read")
                            .redirect_uri(authCallbackUri)
                            .show_dialog(true)
                            .build();
            final CompletableFuture<URI> uriFuture = authorizationCodeUriRequest.executeAsync();

            // Thread free to do other tasks...
            // Example Only. Never block in production code.
            final var uri = uriFuture.join();

            log.info("URI: {}", uri.toString());
        } catch (CompletionException e) {
            log.error("Error: {}", e.getCause().getMessage(), e);
        } catch (CancellationException e) {
            log.error("Async operation cancelled.");
        }
    }

    public String requestAuthorization(String code) {
        var authorizationCodeRequest =
                spotifyApi
                        .authorizationCode(code)
                        .redirect_uri(authCallbackUri)
                        .build();

        try {
            final var authorizationCodeCredentials =
                    authorizationCodeRequest.execute();

            spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
            spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());

            log.info("Expires in: {} seconds", authorizationCodeCredentials.getExpiresIn());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            log.error("Error ", e);
        }

        return spotifyApi.getAccessToken();
    }
    // ============================== Spotify util code ==============================
    public Paging<Artist> executeGetUsersTopArtists(final int offset) throws IOException, ParseException, SpotifyWebApiException {
        return spotifyApi.getUsersTopArtists()
                .offset(offset)
                .limit(10)
                .build().execute();
    }
}