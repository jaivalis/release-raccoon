package com.raccoon.user.dto;

public record UserProfile(
        String id,
        boolean spotifyEnabled,
        String lastfmUsername,
        boolean unsubscribed,
        int notifyIntervalDays
) {}