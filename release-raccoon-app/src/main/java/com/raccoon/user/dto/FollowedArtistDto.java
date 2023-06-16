package com.raccoon.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * Artist Entity projection, used for UserProfile projection
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(NON_NULL)
@Data
// todo: maybe rename or extract parent class
// todo: record?
public class FollowedArtistDto {

    @NotNull
    private Long id;

    @NotNull
    private String name;

    private String lastfmUri;

    private String spotifyUri;

    private String musicbrainzId;

}
