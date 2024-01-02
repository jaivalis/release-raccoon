package com.raccoon.scraper.config;

import io.smallrye.config.ConfigMapping;
import jakarta.resource.spi.ConfigProperty;

@ConfigMapping(prefix = "musicbrainz")
public interface MusicbrainzConfig {

    @ConfigProperty(defaultValue = "5.0")
    Double queriesPerSecond();

}
