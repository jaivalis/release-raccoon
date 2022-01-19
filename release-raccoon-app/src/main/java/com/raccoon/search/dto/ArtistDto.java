package com.raccoon.search.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

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

}
