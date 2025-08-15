package com.raccoon.configuration;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "raccoon")
public interface RaccoonConfig {

    String baseUrl();

}
