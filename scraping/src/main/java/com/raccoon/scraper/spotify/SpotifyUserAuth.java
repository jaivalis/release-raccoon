package com.raccoon.scraper.spotify;

import com.raccoon.scraper.config.SpotifyConfig;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.net.URI;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Slf4j
@ApplicationScoped
public class SpotifyUserAuth {
    // https://github.com/thelinmichael/spotify-web-api-java/blob/master/examples/authorization/authorization_code/AuthorizationCodeUriExample.java

    private final String clientId;
    private final String clientSecret;

    private static final URI redirectUri = SpotifyHttpManager.makeUri("https://example.com/spotify-redirect");

    public SpotifyUserAuth(SpotifyConfig config) {
        clientId = config.getClientId();
        clientSecret = config.getClientSecret();
    }

    private SpotifyApi spotifyApi;

    @PostConstruct
    private void init() {
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRedirectUri(redirectUri)
                .build();
    }

//    private static final AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
//          .state("x4xkmn9pu3j6ukrs8n")
//          .scope("user-read-birthdate,user-read-email")
//          .show_dialog(true)
//            .build();

    public void authorizationCodeUri_Sync() {
        AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
                .state("x4xkmn9pu3j6ukrs8n")
                .scope("user-top-read")
                .show_dialog(true)
                .build();
        final URI uri = authorizationCodeUriRequest.execute();

        log.info("URI: " + uri.toString());
    }

    public void authorizationCodeUri_Async() {
        try {
            AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
                    .state("x4xkmn9pu3j6ukrs8n")
                    .scope("user-top-read")
                    .show_dialog(true)
                    .build();
            final CompletableFuture<URI> uriFuture = authorizationCodeUriRequest.executeAsync();

            // Thread free to do other tasks...

            // Example Only. Never block in production code.
            final URI uri = uriFuture.join();

            log.info("URI: " + uri.toString());
        } catch (CompletionException e) {
            log.error("Error: " + e.getCause().getMessage());
        } catch (CancellationException e) {
            log.error("Async operation cancelled.");
        }
    }

//    public static void main(String[] args) {
//        authorizationCodeUri_Sync();
//        authorizationCodeUri_Async();
//    }
}