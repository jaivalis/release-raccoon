package com.raccoon.scraper.musicbrainz.dto;

import java.util.List;

import javax.json.bind.annotation.JsonbProperty;

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
        @JsonbProperty("status-id")
        private String statusId;
        @JsonbProperty("packaging-id")
        private String packagingId;
        private int count;
        private String title;
        private String status;
        private String packaging;
        @JsonbProperty("artist-credit")
        private List<ArtistCredit> artistCredits;
        private String date;
        @JsonbProperty("release-group")
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
        @JsonbProperty("sort-name")
        private String sortName;
    }

    @Data
    public static class ReleaseGroup {
        private String id;
        private String typeId;
        private String title;
        @JsonbProperty("primary-type")
        private String primaryType;
    }

}
