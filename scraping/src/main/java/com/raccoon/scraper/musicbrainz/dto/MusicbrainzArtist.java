package com.raccoon.scraper.musicbrainz.dto;

import javax.json.bind.annotation.JsonbProperty;

import lombok.Data;

@Data
public class MusicbrainzArtist {

    private String id;
    private int score;
    private String type;
    @JsonbProperty("type-id")
    private String typeId;
    private String name;
    @JsonbProperty("sort-name")
    private String sortName;
    private String gender;
    private String country;
    private String disambiguation;

}
