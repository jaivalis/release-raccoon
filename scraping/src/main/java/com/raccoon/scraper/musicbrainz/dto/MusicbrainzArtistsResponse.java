package com.raccoon.scraper.musicbrainz.dto;

import java.util.List;

import lombok.Data;

/**
 * Built after the Musicbrainz documentation:
 *                                            https://musicbrainz.org/doc/Artist
 */
@Data
public class MusicbrainzArtistsResponse {

    private int count;
    private int offset;

    private String created;
    private List<MusicbrainzArtist> artists;

}
