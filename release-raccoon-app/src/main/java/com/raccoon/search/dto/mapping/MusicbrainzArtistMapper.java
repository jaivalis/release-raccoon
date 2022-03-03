package com.raccoon.search.dto.mapping;

import com.raccoon.scraper.musicbrainz.dto.MusicbrainzArtist;
import com.raccoon.search.dto.ArtistDto;

import javax.enterprise.context.ApplicationScoped;

/**
 * Makes a projection of MusicbrainzArtistsResponse.MusicbrainzArtist objects to be returned as search results
 */
@ApplicationScoped
public class MusicbrainzArtistMapper {

    public ArtistDto toDto(MusicbrainzArtist artist) {
        return ArtistDto.builder()
                .name(artist.getName())
                .musicbrainzId(artist.getId())
                .lastfmUri("https://www.last.fm/music/" + artist.getName().replace(" ", "+"))
                .spotifyUri("")
                .build();
    }

}
