package com.raccoon.scraper.config;

import io.quarkus.arc.config.ConfigProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.resource.spi.ConfigProperty;

@Data
@NoArgsConstructor
@ConfigProperties(prefix = "last.fm")
public class LastFmConfig {

    @ConfigProperty
    String apiKey;

    @ConfigProperty
    String applicationName;

    @ConfigProperty
    String sharedSecret;

}
