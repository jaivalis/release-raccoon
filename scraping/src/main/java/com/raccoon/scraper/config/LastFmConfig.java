package com.raccoon.scraper.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "last.fm")
public interface LastFmConfig {

    String apiKey();

    String applicationName();

    String sharedSecret();

}
