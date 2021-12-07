package com.raccoon.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReleaseTest {

    Release release;

    @Test
    void getSpotifyUriId() {
        release = new Release();
        release.setSpotifyUri("spotify:album:xyz");

        assertEquals("xyz", release.getSpotifyUriId());
    }

    @Test
    void getSpotifyUriBadInput() {
        release = new Release();
        release.setSpotifyUri("spotify:album:");

        assertEquals("", release.getSpotifyUriId());
    }

    @Test
    void getSpotifyUriIdNull() {
        release = new Release();
        release.setSpotifyUri(null);

        assertEquals("", release.getSpotifyUriId());
    }
}