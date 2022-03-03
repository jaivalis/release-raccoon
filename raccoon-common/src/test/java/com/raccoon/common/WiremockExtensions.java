package com.raccoon.common;

import com.github.tomakehurst.wiremock.WireMockServer;

import java.util.Collections;
import java.util.Map;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import lombok.extern.slf4j.Slf4j;

/**
 * The mocks are located in `resources/mappings/stub.json`
 * Currently this class is responsible for stubbing the Musicbrainz responses alone.
 */
@Slf4j
public class WiremockExtensions implements QuarkusTestResourceLifecycleManager {

    private WireMockServer wireMockServer;

    @Override
    public Map<String, String> start() {
        wireMockServer = new WireMockServer();
        wireMockServer.start();

        return Collections.singletonMap(
                "quarkus.rest-client.\"com.raccoon.scraper.musicbrainz.MusicbrainzService\".url", wireMockServer.baseUrl()
        );
    }

    @Override
    public void stop() {
        if (null != wireMockServer) {
            wireMockServer.stop();
        }
    }

}
