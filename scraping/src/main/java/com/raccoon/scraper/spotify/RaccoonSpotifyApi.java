package com.raccoon.scraper.spotify;

import com.raccoon.scraper.config.SpotifyConfig;

import org.apache.hc.core5.http.ParseException;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.requests.data.browse.GetListOfNewReleasesRequest;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.constraints.Max;
import lombok.extern.slf4j.Slf4j;

/**
 * Holds logic for querying spotify
 */
@Slf4j
@ApplicationScoped
public class RaccoonSpotifyApi {

    SpotifyApi spotifyApi;

    private long credentialsExpiryTs = 0L;

    @Max(50)
    static final int DEFAULT_LIMIT = 50;

    public RaccoonSpotifyApi(final SpotifyConfig config) {
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(config.clientId())
                .setClientSecret(config.clientSecret())
                .build();
    }

    Paging<AlbumSimplified> fetchNewReleasesPaginated(final int offset)
            throws ParseException, SpotifyWebApiException, IOException, InterruptedException {
        if (System.currentTimeMillis() > credentialsExpiryTs) {
            validateClientCredentials();
        }
        return createNewReleasesRequest(offset).execute();
    }

    private GetListOfNewReleasesRequest createNewReleasesRequest(int offset) {
        return spotifyApi.getListOfNewReleases()
                .offset(offset)
                .limit(DEFAULT_LIMIT)
                .build();
    }

    private void validateClientCredentials() throws InterruptedException {
        try {
            var clientCredentialsRequest = spotifyApi.clientCredentials()
                    .build();
            final CompletableFuture<ClientCredentials> clientCredentialsFuture = clientCredentialsRequest.executeAsync();

            final var clientCredentials = clientCredentialsFuture.get();
            credentialsExpiryTs = System.currentTimeMillis() + clientCredentials.getExpiresIn() * 1000;

            // Set access token for further "spotifyApi" object usage
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());

            log.info("Spotify client credentials expire in: {}", clientCredentials.getExpiresIn());
        } catch (CompletionException | CancellationException | ExecutionException e) {
            log.error("Could not validate credentials", e);
        }
    }

}
