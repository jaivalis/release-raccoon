package com.raccoon.scraper.mapper;

import com.raccoon.entity.Release;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import se.michaelthelin.spotify.enums.AlbumType;
import se.michaelthelin.spotify.enums.ModelObjectType;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpotifyReleaseMapperTest {

    SpotifyReleaseMapper mapper = new SpotifyReleaseMapper();

    @ParameterizedTest
    @EnumSource(AlbumType.class)
    void fromAlbumSimplified(AlbumType type) {
        var uri = "releaseUri";
        var releaseDate = LocalDate.now();
        var albumSimplified = new AlbumSimplified.Builder()
                .setId("1")
                .setArtists(
                        new ArtistSimplified.Builder()
                                .setName("artistName")
                                .setUri("artistUri")
                                .build()
                )
                .setType(ModelObjectType.ALBUM)
                .setAlbumType(type)
                .setUri(uri)
                .setReleaseDate(releaseDate.toString())
                .build();

        Release release = mapper.fromAlbumSimplified(albumSimplified);

        assertEquals(type.toString(), release.getType());
        assertEquals(uri, release.getSpotifyUri());
        assertEquals(releaseDate, release.getReleasedOn());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2020", "2020-30-30", "garbage"})
    @DisplayName("fromAlbumSimplified(): Bad date provided, should fallback to today")
    void fromAlbumSimplifiedBadDate(String invalidDates) {
        var albumSimplified = new AlbumSimplified.Builder()
                .setId("1")
                .setArtists()
                .setType(ModelObjectType.ALBUM)
                .setAlbumType(AlbumType.ALBUM)
                .setUri("releaseUri")
                .setReleaseDate(invalidDates)
                .build();

        Release release = mapper.fromAlbumSimplified(albumSimplified);

        var now = LocalDate.now();
        assertEquals(now.getYear(), release.getReleasedOn().getYear());
        assertEquals(now.getMonth(), release.getReleasedOn().getMonth());
        assertEquals(now.getDayOfMonth(), release.getReleasedOn().getDayOfMonth());
    }

}
