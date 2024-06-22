package com.raccoon.integration.profile;

import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

public class TasteScrapeDatabaseProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
                "quarkus.hibernate-orm.sql-load-script", "import-taste-scrape.sql"
        );
    }

}
