package com.raccoon.search.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.raccoon.entity.Artist;

import de.umass.lastfm.ImageSize;

import lombok.Getter;

@Getter
public class ArtistDto {

    @JsonProperty
    public String id;
    public String name;
    public String lastfmUri;
    public String spotifyUri;
    public String imageUri;

    public ArtistDto(Artist artist) {
        this.id = artist.id + "";
        this.name = artist.getName();
        this.lastfmUri = artist.getLastfmUri();
        this.spotifyUri = artist.getSpotifyUri();
    }

    public ArtistDto(de.umass.lastfm.Artist artist) {
        this.id = artist.getId();
        this.name = artist.getName();
        this.lastfmUri = artist.getUrl();
        this.imageUri = artist.getImageURL(ImageSize.SMALL);
        this.spotifyUri = "";
    }

}
