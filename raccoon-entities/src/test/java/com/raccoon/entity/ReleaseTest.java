package com.raccoon.entity;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.raccoon.entity.Constants.SPOTIFY_RELEASE_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    void isCreditedToArtist() {
        release = new Release();

        assertThat(release.isCreditedToArtist(List.of()))
                .isFalse();
    }

    @Test
    void isCreditedToArtist_should_returnFalse_when_listNull() {
        release = new Release();

        assertThat(release.isCreditedToArtist((List) null))
                .isFalse();
    }

    @Test
    void testIsCreditedToArtist() {
        release = Instancio.create(Release.class);

        assertThat(release.isCreditedToArtist(release.getArtists().get(0)))
                .isTrue();
    }

    @Test
    void testIsCreditedToArtistList_should_returnTrue_when_entireList() {
        release = Instancio.create(Release.class);

        assertThat(release.isCreditedToArtist(release.getArtists()))
                .isTrue();
    }

    @Test
    void isCreditedToArtist_should_returnFalse_when_null() {
        release = new Release();

        assertThat(release.isCreditedToArtist((Artist) null))
                .isFalse();
    }
}