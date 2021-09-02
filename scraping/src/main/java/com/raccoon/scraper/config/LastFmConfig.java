package com.raccoon.scraper.config;

import javax.resource.spi.ConfigProperty;

import io.smallrye.config.ConfigMapping;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ConfigMapping(prefix = "last.fm")
public class LastFmConfig {

    @ConfigProperty
    String apiKey;

    @ConfigProperty
    String applicationName;

    @ConfigProperty
    String sharedSecret;

}
