package com.raccoon.search.dto.mapping;

import com.raccoon.scraper.musicbrainz.dto.MusicbrainzArtist;
import com.raccoon.search.dto.ArtistDto;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class MusicbrainzArtistMapperTest {

    MusicbrainzArtistMapper mapper = new MusicbrainzArtistMapper();

    @Test
    void toDto() {
        MusicbrainzArtist artist = Mockito.mock(MusicbrainzArtist.class);
        when(artist.getName()).thenReturn("zapp franka");
        when(artist.getId()).thenReturn("id");

        ArtistDto project = mapper.toDto(artist);

        assertEquals("zapp franka", project.getName());
        assertEquals("id", project.getMusicbrainzId());
        assertEquals("https://www.last.fm/music/zapp+franka", project.getLastfmUri());
    }
}