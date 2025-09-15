package com.raccoon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.raccoon.common.StringUtil.isNullOrEmpty;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(NON_NULL)
@Data
public class ArtistDto {

    @NotNull
    private Long id;

    @NotNull
    private String name;

    private String lastfmUri;

    private String spotifyUri;

    private String musicbrainzId;

    @Builder.Default
    private Integer followerCount = 0;

    public String getLastfmUri() {
        return isNullOrEmpty(lastfmUri) ? formatLastfmUri() : lastfmUri;
    }

    private String formatLastfmUri() {
        return String.format("https://www.last.fm/music/%s", name.replaceAll("\\s+", "+"));
    }

}
