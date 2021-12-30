package com.raccoon.entity;

import org.junit.jupiter.api.Test;

import static com.raccoon.entity.Constants.SPOTIFY_RELEASE_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ReleaseTest {

    Release release;

    @Test
    void getSpotifyUriId() {
        release = new Release();
        release.setSpotifyUri(SPOTIFY_RELEASE_PREFIX + "3TsEEdpuuCN1G0dPxV4uOA");

        assertEquals("3TsEEdpuuCN1G0dPxV4uOA", release.getSpotifyUriId());
    }

    @Test
    void getSpotifyUriBadInput() {
        release = new Release();
        release.setSpotifyUri(SPOTIFY_RELEASE_PREFIX);

        assertEquals("", release.getSpotifyUriId());
    }

    @Test
    void getSpotifyUriIdNull() {
        release = new Release();
        release.setSpotifyUri(null);

        assertEquals("", release.getSpotifyUriId());
    }
}