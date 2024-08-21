package com.raccoon.user;

import java.util.List;
import java.util.Optional;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "redirect")
public interface RedirectConfig {

    Optional<List<String>> getWhitelistedUrls();

}
