package com.raccoon.search.dto;

import de.umass.lastfm.Artist;

import javax.enterprise.context.ApplicationScoped;

/**
 * Makes a projection of de.umass.lastfm.Artist objects to be returned as search results
 */
@ApplicationScoped
public class LastFmArtistMapper {

    public ArtistDto toDto(Artist artist) {
        return ArtistDto.builder()
                .name(artist.getName())
                .lastfmUri(artist.getUrl())
                .spotifyUri("")
                .build();
    }

}
