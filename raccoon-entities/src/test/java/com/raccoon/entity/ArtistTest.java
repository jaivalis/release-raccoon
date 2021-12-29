package com.raccoon.entity;

import org.junit.jupiter.api.Test;

import static com.raccoon.entity.Constants.SPOTIFY_ARTIST_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ArtistTest {

    Artist artist;

    @Test
    void getSpotifyUriId() {
        artist = new Artist();
        artist.setSpotifyUri(SPOTIFY_ARTIST_PREFIX + "xyz");

        assertEquals("xyz", artist.getSpotifyUriId());
    }

    @Test
    void getSpotifyUriBadInput() {
        artist = new Artist();
        artist.setSpotifyUri(SPOTIFY_ARTIST_PREFIX);

        assertEquals("", artist.getSpotifyUriId());
    }

    @Test
    void getSpotifyUriIdNull() {
        artist = new Artist();
        artist.setSpotifyUri(null);

        assertEquals("", artist.getSpotifyUriId());
    }

}