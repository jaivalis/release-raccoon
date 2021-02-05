package com.raccoon;

import org.testcontainers.containers.MariaDBContainer;

import java.util.HashMap;
import java.util.Map;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

@QuarkusTestResource(TestContainersMariaDbTestResource.Initializer.class)
public class TestContainersMariaDbTestResource {

    public static class Initializer implements QuarkusTestResourceLifecycleManager {

        private MariaDBContainer<?> mariaDbContainer;

        @Override
        public Map<String, String> start() {
            this.mariaDbContainer = new MariaDBContainer<>("mariadb:latest");//.withDatabaseName("test");
            mariaDbContainer.start();

            return configutationParameters();
        }

        @Override
        public void stop() {
            if (this.mariaDbContainer != null) {
                this.mariaDbContainer.close();
            }
        }

        private Map<String, String> configutationParameters() {
            final Map<String, String> conf = new HashMap<>();
            conf.put("quarkus.datasource.jdbc.url", this.mariaDbContainer.getJdbcUrl());
            conf.put("quarkus.datasource.username", this.mariaDbContainer.getUsername());
            conf.put("quarkus.datasource.password", this.mariaDbContainer.getPassword());

            conf.put("quarkus.hibernate-orm.database.generation", "drop-and-create");

            return conf;
        }
    }
}
