package com.raccoon.user.dto;

import com.raccoon.dto.ArtistDto;

import java.time.LocalDate;
import java.util.List;

public record FollowedArtistsRelease(
        Long id,
        String name,
        String type,
        String spotifyUri,
        String musicbrainzId,
        LocalDate releasedOn,
        List<ArtistDto> artists
) {
}