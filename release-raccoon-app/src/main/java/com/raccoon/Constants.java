package com.raccoon;

public class Constants {

    private Constants() {
        // Hiding constructor
    }

    // Keycloak claims
    public static final String EMAIL_CLAIM = "email";
    public static final String USERNAME_CLAIM = "preferred_username";
    public static final String LASTFM_USERNAME_CLAIM = "lastfm_username";
    public static final String SPOTIFY_ENABLED_CLAIM = "spotify_enabled";

    public static final String RELEASE_SCRAPE_RESPONSE_SUCCESS_TEMPLATE = "Successfully scraped %d releases";
    public static final String RELEASE_SCRAPE_RESPONSE_FAILURE_TEMPLATE = "Failed to scrape new releases. Cause: %s";

    // Searchers
    public static final String HIBERNATE_SEARCHER_ID = "Db";
    public static final String MUSICBRAINZ_SEARCHER_ID = "Musicbrainz";
    public static final String LASTFM_SEARCHER_ID = "Lastfm";
    public static final Double HIBERNATE_SEARCHER_TRUSTWORTHINESS = 1.0;
    public static final Double MUSICBRAINZ_SEARCHER_TRUSTWORTHINESS = 0.8;
    public static final Double LASTFM_SEARCHER_TRUSTWORTHINESS = 0.5;

}
