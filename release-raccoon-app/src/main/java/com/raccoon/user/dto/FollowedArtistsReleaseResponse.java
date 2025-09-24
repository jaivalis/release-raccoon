package com.raccoon.user.dto;

import java.util.List;

public record FollowedArtistsReleaseResponse(
        int total,
        List<FollowedArtistsRelease> releases
) {
}