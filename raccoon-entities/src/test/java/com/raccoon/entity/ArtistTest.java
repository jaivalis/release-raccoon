package com.raccoon.entity;

import org.junit.jupiter.api.Test;

import static com.raccoon.entity.Constants.SPOTIFY_ARTIST_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ArtistTest {

    Artist artist;

    @Test
    void getSpotifyUriId() {
        artist = new Artist();
        artist.setSpotifyUri(SPOTIFY_ARTIST_PREFIX + "3TsEEdpuuCN1G0dPxV4uOA");

        assertEquals("3TsEEdpuuCN1G0dPxV4uOA", artist.getSpotifyUriId());
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

    @Test
    void equals_should_returnTrue() {
        Artist artist1 = new Artist();
        artist1.setId(1L);
        artist1.setName("Artist Name");

        Artist artist2 = new Artist();
        artist2.setId(1L);
        artist2.setName("Artist Name");

        assertThat(artist1).isEqualTo(artist2);
        assertThat(artist1).isNotEqualTo(new Object());
        assertThat(artist1).isEqualTo(artist1);
    }

    @Test
    void equals_should_returnFalse_when_nonArtist() {
        Artist artist1 = new Artist();
        artist1.setId(1L);
        artist1.setName("Artist Name");

        assertThat(artist1).isNotEqualTo(new Object());
    }

    @Test
    void equals_checkEquality_worksCorrectly() {
        // prepare two identical Artist objects
        Artist artist1 = new Artist();
        artist1.setId(1L);
        artist1.setName("Artist Name");

        // prepare an Artist object with a different id
        Artist artist2 = new Artist();
        artist2.setId(2L);
        artist2.setName("Artist Name");

        // prepare an Artist object with a different name
        Artist artist3 = new Artist();
        artist3.setId(1L);
        artist3.setName("Different Name");

        assertThat(artist1).isNotEqualTo(artist3); // test second if branch (different id)
        assertThat(artist1).isNotEqualTo(artist3); // test second if branch (different name)
    }
}