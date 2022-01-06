package com.raccoon.common;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.time.Duration;
import java.util.Map;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import lombok.extern.slf4j.Slf4j;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;

@Slf4j
public class ElasticSearchTestResource implements QuarkusTestResourceLifecycleManager {

    private static final String ELASTICSEARCH_IMAGE = "docker.elastic.co/elasticsearch/elasticsearch-oss";
    private static final String ELASTICSEARCH_VERSION = "7.10.2";

    private ElasticsearchContainer container;

    @Override
    public Map<String, String> start() {
        log.info(TestcontainersConfiguration.getInstance().toString());

        var architecture = System.getProperty("os.arch");
        var version = switch (architecture) {
            case "amd64" -> ELASTICSEARCH_VERSION;
            case "x86_64" -> ELASTICSEARCH_VERSION + "-arm64";
            default -> throw new RuntimeException(String.format("Unsupported cpu architecture %s detected", System.getProperty("os.arch")));
        };

        try {
            container = new ElasticsearchContainer(
                    DockerImageName
                            .parse(ELASTICSEARCH_IMAGE)
                            .withTag(version)
            )
                    .withExposedPorts(9200, 9300)
                    .withEnv("discovery.type", "single-node")
                    .waitingFor(
                            new HttpWaitStrategy()
                                    .forPort(9200)
                                    .forStatusCodeMatching(response -> response == HTTP_OK || response == HTTP_UNAUTHORIZED)
                                    .withStartupTimeout(Duration.ofMinutes(2))
                    );
            container.start();

            String host = container.getHost();
            Integer port = container.getMappedPort(9200);
            log.info("Started ElasticSearch TestContainer on {}:{}", host, port);

            return Map.of(
                    "quarkus.hibernate-search-orm.elasticsearch.version", "7",
                    "quarkus.hibernate-search-orm.elasticsearch.hosts", host + ":" + port,
                    "quarkus.hibernate-search-orm.elasticsearch.analysis.configurer", "bean:raccoonAnalysisConfigurer",
                    "quarkus.hibernate-search-orm.schema-management.strategy", "drop-and-create",
                    "quarkus.hibernate-search-orm.automatic-indexing.synchronization.strategy", "sync"
            );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        try {
            if (container != null) {
                container.stop();
            }
        } catch (Exception e) {
            // Ignored
        }
    }
}
