package com.raccoon.profile;

import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

public class ArtistResourceDatabaseProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
                "quarkus.hibernate-orm.sql-load-script", "import-artist-resource.sql"
        );
    }

}
