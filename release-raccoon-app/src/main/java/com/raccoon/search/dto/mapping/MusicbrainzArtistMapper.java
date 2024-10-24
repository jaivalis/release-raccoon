package com.raccoon.search.dto.mapping;

import com.raccoon.scraper.musicbrainz.dto.MusicbrainzArtist;
import com.raccoon.search.dto.SearchResultArtistDto;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Makes a projection of MusicbrainzArtistsResponse.MusicbrainzArtist objects to be returned as search results
 */
@ApplicationScoped
public class MusicbrainzArtistMapper {

    public SearchResultArtistDto toDto(MusicbrainzArtist artist) {
        return SearchResultArtistDto.builder()
                .name(artist.getName().trim())
                .musicbrainzId(artist.getId())
                .lastfmUri("https://www.last.fm/music/" + artist.getName().replace(" ", "+"))
                .build();
    }

}
