package com.raccoon.search.dto;

import com.raccoon.entity.Artist;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArtistDtoProjectorTest {

    ArtistDtoProjector projector = new ArtistDtoProjector();

    @Test
    void projectEntity() {
        Artist artist = new Artist();
        artist.setName("name");
        artist.setId(6L);
        artist.setSpotifyUri("uri");
        artist.setLastfmUri("uri2");

        ArtistDto project = projector.project(artist);

        assertEquals("name", project.getName());
        assertEquals("6", project.getId());
        assertEquals("uri", project.getSpotifyUri());
        assertEquals("uri2", project.getLastfmUri());
    }

    @Test
    void projectLastfmObj() {
        de.umass.lastfm.Artist artist = Mockito.mock(de.umass.lastfm.Artist.class);
        when(artist.getName()).thenReturn("name");
        when(artist.getId()).thenReturn("9");
        when(artist.getUrl()).thenReturn("uri");

        ArtistDto project = projector.project(artist);

        assertEquals("name", project.getName());
        assertEquals("9", project.getId());
        assertEquals("uri", project.getLastfmUri());
    }
}