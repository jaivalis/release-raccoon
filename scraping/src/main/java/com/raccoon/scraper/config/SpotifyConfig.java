package com.raccoon.scraper.config;

import io.quarkus.arc.config.ConfigProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.resource.spi.ConfigProperty;

@Data
@NoArgsConstructor
@ConfigProperties(prefix = "spotify")
public class SpotifyConfig {

    @ConfigProperty
    String clientId;

    @ConfigProperty
    String clientSecret;

}
