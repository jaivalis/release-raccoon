package com.raccoon.scraper.config;

import javax.resource.spi.ConfigProperty;

import io.smallrye.config.ConfigMapping;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ConfigMapping(prefix = "spotify")
public class SpotifyConfig {

    @ConfigProperty
    String clientId;

    @ConfigProperty
    String clientSecret;

}
