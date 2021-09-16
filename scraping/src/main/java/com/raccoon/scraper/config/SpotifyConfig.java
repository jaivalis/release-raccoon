package com.raccoon.scraper.config;

import javax.validation.constraints.NotNull;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "spotify")
public interface SpotifyConfig {

    String clientId();

    String clientSecret();

    @NotNull
    String authCallbackUri();
}
