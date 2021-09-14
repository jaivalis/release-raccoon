package com.raccoon.entity.repository;

import com.raccoon.entity.Artist;

import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.transaction.Transactional;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
class ArtistRepositoryTest {

    @Inject
    ArtistRepository repository;

    @Test
    @Transactional
    void findByNameEmpty() {
        var name = "does not exist";

        final var found = repository.findByNameOptional(name);

        assertTrue(found.isEmpty());
    }

    @Test
    @Transactional
    void findByNameOptional() {
        var name = "name";
        var artist = new Artist();
        artist.setName(name);
        repository.persist(artist);

        final var found = repository.findByNameOptional(name);

        assertTrue(found.isPresent());
        assertEquals(name, found.get().getName());
    }
}