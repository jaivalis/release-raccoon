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
 * Artist Entity projection, used for SearchResults (allows for the id to be null)
 */
@Builder
@JsonInclude(NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SearchResultArtistDto {

    private Long id;

    @NotNull
    private String name;

    private String lastfmUri;

    private String spotifyUri;

    private String musicbrainzId;

    /**
     * True if the raccoonUser searching already follows the artist
     */
    private boolean followedByUser = false;

    /**
     * Pull nullable fields of other into this
     * @param other Other SearchResultArtistDto to merge from
     */
    public void merge(SearchResultArtistDto other) {
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
        SearchResultArtistDto artistDto = (SearchResultArtistDto) o;
        return name.equals(artistDto.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

}
