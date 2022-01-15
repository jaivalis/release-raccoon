package com.raccoon.search.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@Builder
@ToString
@JsonInclude(NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public class ArtistDto {

    private String id;
    private String name;
    private String lastfmUri;
    private String spotifyUri;

}
