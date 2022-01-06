package com.raccoon.search.dto;

import com.raccoon.entity.Artist;

import javax.enterprise.context.ApplicationScoped;

/**
 * Makes a projection of Artist objects to be returned as search results
 */
@ApplicationScoped
public class ArtistDtoProjector {

    public ArtistDto project(Artist artist) {
        return ArtistDto.builder()
                .id(artist.id + "")
                .name(artist.getName())
                .lastfmUri(artist.getLastfmUri())
                .spotifyUri(artist.getSpotifyUri())
                .build();
    }

    public ArtistDto project(de.umass.lastfm.Artist artist) {
        return ArtistDto.builder()
                .id(artist.getId() + "")
                .name(artist.getName())
                .lastfmUri(artist.getUrl())
                .spotifyUri("")
                .build();
    }

}
