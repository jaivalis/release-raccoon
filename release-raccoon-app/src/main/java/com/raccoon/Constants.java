package com.raccoon;

public class Constants {

    private Constants() {
        // Hiding the implicit one
    }

    // Keycloak claims
    public static final String EMAIL_CLAIM = "email";
    public static final String USERNAME_CLAIM = "preferred_username";
    public static final String LASTFM_USERNAME_CLAIM = "lastfm_username";
    public static final String SPOTIFY_ENABLED_CLAIM = "spotify_enabled";

    public static final String RELEASE_SCRAPE_RESPONSE_SUCCESS_TEMPLATE = "Successfully scraped %d releases";
    public static final String RELEASE_SCRAPE_RESPONSE_FAILURE_TEMPLATE = "Failed to scrape new releases. Cause: %s";

    // Searcher IDs
    public static final String HIBERNATE_SEARCHER_ID = "fromDb";
    public static final String LASTFM_SEARCHER_ID = "fromLastfm";
}
