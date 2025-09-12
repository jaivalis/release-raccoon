package com.raccoon.search.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

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
    @Builder.Default
    private boolean followedByUser = false;

    @Builder.Default
    private Integer followerCount = 0;

    /**
     * Pull nullable fields of other into this
     * @param other Other SearchResultArtistDto to merge from
     */
    public void merge(SearchResultArtistDto other) {
        if (isNull(lastfmUri) && nonNull(other.lastfmUri)) {
            lastfmUri = other.lastfmUri;
        }

        if (isNull(musicbrainzId) && nonNull(other.musicbrainzId)) {
            musicbrainzId = other.musicbrainzId;
        }

        if (!followedByUser && other.followedByUser) {
            followedByUser = true;
        }

        if (isNull(followerCount) && nonNull(other.followerCount)) {
            followerCount = other.followerCount;
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
