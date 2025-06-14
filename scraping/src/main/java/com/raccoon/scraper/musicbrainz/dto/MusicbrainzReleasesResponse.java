package com.raccoon.scraper.musicbrainz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import lombok.Data;

/**
 * Built after the Musicbrainz documentation:
 *                                            https://musicbrainz.org/doc/Release
 */
@Data
public class MusicbrainzReleasesResponse {

    private int count;
    private int offset;

    private String created;
    private List<MusicbrainzRelease> releases;

    private String error;

    @Data
    public static class MusicbrainzRelease {
        private String id;
        private int score;
        @JsonProperty("status-id")
        private String statusId;
        @JsonProperty("packaging-id")
        private String packagingId;
        private int count;
        private String title;
        private String status;
        private String packaging;
        @JsonProperty("artist-credit")
        private List<ArtistCredit> artistCredits;
        private String date;
        @JsonProperty("release-group")
        private ReleaseGroup releaseGroup;
    }

    @Data
    public static class ArtistCredit {
        private String name;
        private MusicBrainsArtist artist;
    }

    @Data
    public static class MusicBrainsArtist {
        private String id;
        private String name;
        @JsonProperty("sort-name")
        private String sortName;
    }

    @Data
    public static class ReleaseGroup {
        private String id;
        private String typeId;
        private String title;
        @JsonProperty("primary-type")
        private String primaryType;
    }

}
