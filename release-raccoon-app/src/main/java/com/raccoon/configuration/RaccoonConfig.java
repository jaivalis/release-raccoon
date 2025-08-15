package com.raccoon.configuration;

import io.smallrye.config.ConfigMapping;
import jakarta.validation.constraints.NotNull;

@ConfigMapping(prefix = "raccoon")
public interface RaccoonConfig {

    @NotNull
    String baseUrl();

}
