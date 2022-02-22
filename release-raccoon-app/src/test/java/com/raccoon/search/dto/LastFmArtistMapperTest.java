package com.raccoon.search.dto;

import com.raccoon.search.dto.mapping.LastFmArtistMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LastFmArtistMapperTest {

    LastFmArtistMapper mapper = new LastFmArtistMapper();

    @Test
    void projectLastfmObj() {
        de.umass.lastfm.Artist artist = Mockito.mock(de.umass.lastfm.Artist.class);
        when(artist.getName()).thenReturn("name");
        when(artist.getUrl()).thenReturn("uri");

        ArtistDto project = mapper.toDto(artist);

        assertEquals("name", project.getName());
        assertEquals("uri", project.getLastfmUri());
    }
}