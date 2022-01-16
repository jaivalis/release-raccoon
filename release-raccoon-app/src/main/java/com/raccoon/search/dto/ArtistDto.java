package com.raccoon.search.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

import java.util.Optional;

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

    @JsonSetter(nulls = Nulls.SKIP)
    private String id;
    @NotNull
    private String name;
    private String lastfmUri;
    private String spotifyUri;

    public Optional<Long> validId() {
        if (id != null && !"null".equalsIgnoreCase(id)) {
            try {
                return Optional.of(Long.parseLong(id));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

}
