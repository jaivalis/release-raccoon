package com.raccoon.scraper.config;

import io.smallrye.config.ConfigMapping;
import jakarta.validation.constraints.NotNull;

@ConfigMapping(prefix = "spotify")
public interface SpotifyConfig {

    String clientId();

    String clientSecret();

    @NotNull
    String authCallbackUri();
}
