package com.raccoon.scraper.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "spotify")
public interface SpotifyConfig {

    String clientId();

    String clientSecret();

}
