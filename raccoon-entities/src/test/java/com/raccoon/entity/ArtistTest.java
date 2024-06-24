package com.raccoon.entity;

import org.junit.jupiter.api.Test;

import static com.raccoon.entity.Constants.SPOTIFY_ARTIST_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;

class ArtistTest {

    Artist artist;

    @Test
    void getSpotifyUriId() {
        String spotifyId = "3TsEEdpuuCN1G0dPxV4uOA";
        artist = new Artist();
        artist.setSpotifyUri(SPOTIFY_ARTIST_PREFIX + spotifyId);

        assertThat(artist.getSpotifyUriId())
                .isEqualTo(spotifyId);
    }

    @Test
    void getSpotifyUriId_should_returnEmptyString_when_badInput() {
        artist = new Artist();
        artist.setSpotifyUri(SPOTIFY_ARTIST_PREFIX);

        assertThat(artist.getSpotifyUriId())
                .isEmpty();
    }

    @Test
    void getSpotifyUriId_should_returnEmptyString_when_null() {
        artist = new Artist();
        artist.setSpotifyUri(null);

        assertThat(artist.getSpotifyUriId())
                .isEmpty();
    }

    @Test
    void equals_should_returnTrue() {
        Artist artist1 = new Artist();
        artist1.setId(1L);
        artist1.setName("Artist Name");

        Artist artist2 = new Artist();
        artist2.setId(1L);
        artist2.setName("Artist Name");

        assertThat(artist1)
                .isEqualTo(artist2)
                .isNotEqualTo(new Object())
                .isEqualTo(artist1);
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
        Artist artist1 = new Artist();
        artist1.setId(1L);
        artist1.setName("Artist Name");

        Artist artist2 = new Artist();
        artist2.setId(2L);
        artist2.setName("Artist Name");

        Artist artist3 = new Artist();
        artist3.setId(1L);
        artist3.setName("Different Name");

        assertThat(artist1)
                .isNotEqualTo(artist3)
                .isNotEqualTo(artist3); // test second if branch (different name)
    }
}