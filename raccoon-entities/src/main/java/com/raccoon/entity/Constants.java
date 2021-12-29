package com.raccoon.entity;

import java.util.regex.Pattern;

public class Constants {

    private Constants() {
        // hide implicit constructor
    }

    public static final Pattern SPOTIFY_ARTIST_URI_PATTERN = Pattern.compile("(spotify:artist:[A-Za-z])\\w+");
    public static final Pattern SPOTIFY_RELEASE_URI_PATTERN = Pattern.compile("(spotify:album:[A-Za-z])\\w+");

}
