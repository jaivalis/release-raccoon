package com.raccoon.search.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * Artist Entity projection, used for Search and follow operations
 */
@Builder
@JsonInclude(NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ArtistDto {

    private Long id;

    @NotNull
    private String name;

    private String lastfmUri;

    private String spotifyUri;

    private String musicbrainzId;

    /**
     * True if the user searching already follows the artist
     */
    private boolean followedByUser = false;

    /**
     * Pull nullable fields of other into this
     * @param other Other ArtistDto to merge from
     */
    public void merge(ArtistDto other) {
        if (lastfmUri == null && other.lastfmUri != null) {
            lastfmUri = other.lastfmUri;
        }

        if (musicbrainzId == null && other.musicbrainzId != null) {
            musicbrainzId = other.musicbrainzId;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArtistDto artistDto = (ArtistDto) o;
        return name.equals(artistDto.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

}
