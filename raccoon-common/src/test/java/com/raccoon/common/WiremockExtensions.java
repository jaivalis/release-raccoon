package com.raccoon.common;

import com.github.tomakehurst.wiremock.WireMockServer;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import lombok.extern.slf4j.Slf4j;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 * The mocks are located in `resources/mappings/stub.json`
 * Currently this class is responsible for stubbing the Musicbrainz responses alone.
 */
@Slf4j
public class WiremockExtensions implements QuarkusTestResourceLifecycleManager {

    private WireMockServer wireMockServer;

    @Override
    public Map<String, String> start() {
        wireMockServer = new WireMockServer(options().dynamicPort());
        wireMockServer.start();
        log.info("{} Stub Mappings found, using baseUrl: {}", wireMockServer.getStubMappings().size(), wireMockServer.baseUrl());
        return Collections.singletonMap(
                "quarkus.rest-client.\"com.raccoon.scraper.musicbrainz.MusicbrainzService\".url", wireMockServer.baseUrl()
        );
    }

    @Override
    public void stop() {
        if (Objects.nonNull(wireMockServer)) {
            wireMockServer.stop();
        }
    }

}
