package com.raccoon.scraper.config;

import javax.resource.spi.ConfigProperty;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "musicbrainz")
public interface MusicbrainzConfig {

    @ConfigProperty(defaultValue = "5.0")
    Double queriesPerSecond();

}
