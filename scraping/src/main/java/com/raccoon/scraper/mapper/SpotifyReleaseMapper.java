package com.raccoon.scraper.mapper;

import com.raccoon.entity.Release;

import se.michaelthelin.spotify.enums.AlbumType;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import static java.util.Objects.nonNull;

/**
 * Makes a parses com.wrapper.spotify.model_objects.specification.AlbumSimplified to
 * returned as search results
 */
@Slf4j
@ApplicationScoped
public class SpotifyReleaseMapper {

    public Release fromAlbumSimplified(AlbumSimplified albumSimplified) {
        final var release = new Release();
        AlbumType albumType = albumSimplified.getAlbumType();

        release.setName(albumSimplified.getName());
        release.setType(nonNull(albumType) ? albumType.toString() : "");
        release.setSpotifyUri(albumSimplified.getUri());

        try {
            release.setReleasedOn(LocalDate.parse(albumSimplified.getReleaseDate()));
        } catch (DateTimeParseException e) {
            log.warn("Exception occurred when parsing release date {}, falling back to today",
                    albumSimplified.getReleaseDate());
            release.setReleasedOn(LocalDate.now());
        }

        return release;
    }

}
