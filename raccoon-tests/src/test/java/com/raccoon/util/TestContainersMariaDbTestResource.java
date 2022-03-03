package com.raccoon.util;

import org.testcontainers.containers.MariaDBContainer;

import java.util.Map;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

@QuarkusTestResource(TestContainersMariaDbTestResource.Initializer.class)
public class TestContainersMariaDbTestResource {

    public static class Initializer implements QuarkusTestResourceLifecycleManager {

        private MariaDBContainer<?> mariaDbContainer;

        @Override
        public Map<String, String> start() {
            this.mariaDbContainer = new MariaDBContainer<>("mariadb:latest");
            mariaDbContainer.start();

            return Map.of(
                    "quarkus.datasource.jdbc.url", this.mariaDbContainer.getJdbcUrl(),
                    "quarkus.datasource.username", this.mariaDbContainer.getUsername(),
                    "quarkus.datasource.password", this.mariaDbContainer.getPassword(),
                    "quarkus.hibernate-orm.database.generation", "drop-and-create"
            );
        }

        @Override
        public void stop() {
            if (this.mariaDbContainer != null) {
                this.mariaDbContainer.close();
            }
        }

    }
}
