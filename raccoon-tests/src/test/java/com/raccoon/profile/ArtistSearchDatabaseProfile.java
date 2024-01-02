package com.raccoon.profile;

import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

public class ArtistSearchDatabaseProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
                "quarkus.hibernate-orm.sql-load-script", "import-artist-search.sql"
        );
    }

}
