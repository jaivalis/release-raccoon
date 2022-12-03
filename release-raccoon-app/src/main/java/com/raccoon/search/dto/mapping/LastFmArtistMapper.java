package com.raccoon.search.dto.mapping;

import com.raccoon.search.dto.SearchResultArtistDto;

import de.umass.lastfm.Artist;

import javax.enterprise.context.ApplicationScoped;

/**
 * Makes a projection of de.umass.lastfm.Artist objects to be returned as search results
 */
@ApplicationScoped
public class LastFmArtistMapper {

    public SearchResultArtistDto toDto(Artist artist) {
        return SearchResultArtistDto.builder()
                .name(artist.getName())
                .lastfmUri(artist.getUrl())
                .build();
    }

}
