package com.raccoon.scraper.spotify;

import com.raccoon.scraper.config.SpotifyConfig;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Artist;
import com.wrapper.spotify.model_objects.specification.Paging;

import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class SpotifyUserAuthorizer {

    private static final URI SPOTIFY_AUTH_CALLBACK_URI = URI.create("http://localhost:8080/spotify-auth-callback/");
    private static final URI SPOTIFY_REDIRECT_URI = SpotifyHttpManager.makeUri("https://example.com/spotify-redirect");

    // https://github.com/thelinmichael/spotify-web-api-java/blob/master/examples/authorization/authorization_code/AuthorizationCodeUriExample.java
    // https://developer.spotify.com/documentation/general/guides/authorization-guide/

    private final String clientId;
    private final String clientSecret;

    public SpotifyUserAuthorizer(SpotifyConfig config) {
        clientId = config.getClientId();
        clientSecret = config.getClientSecret();
    }

    private SpotifyApi spotifyApi;

    @PostConstruct
    private void init() {
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRedirectUri(SPOTIFY_REDIRECT_URI)
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
                .redirect_uri(SPOTIFY_AUTH_CALLBACK_URI)
                .show_dialog(true)
                .build();
        final var uri = authorizationCodeUriRequest.execute();

        log.info("URI: {}", uri.toString());

        return uri;
    }

    public void authorizationCodeUriAsync(final String email) {
        try {
            var authorizationCodeUriRequest =
                    spotifyApi.authorizationCodeUri()
                            .state(email)
                            .scope("user-top-read")
                            .redirect_uri(SPOTIFY_AUTH_CALLBACK_URI)
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
                        .redirect_uri(SPOTIFY_AUTH_CALLBACK_URI)
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