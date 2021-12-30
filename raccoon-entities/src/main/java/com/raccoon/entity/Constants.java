package com.raccoon.entity;

import java.util.regex.Pattern;

public class Constants {

    private Constants() {
        // hide implicit constructor
    }

    public static final String SPOTIFY_RELEASE_PREFIX = "spotify:album:";
    public static final String SPOTIFY_ARTIST_PREFIX = "spotify:artist:";

    public static final Pattern SPOTIFY_ARTIST_URI_PATTERN = Pattern.compile("(" + SPOTIFY_ARTIST_PREFIX + "[A-Za-z0-9])\\w+");
    public static final Pattern SPOTIFY_RELEASE_URI_PATTERN = Pattern.compile("(" + SPOTIFY_RELEASE_PREFIX + "[A-Za-z0-9])\\w+");

}
