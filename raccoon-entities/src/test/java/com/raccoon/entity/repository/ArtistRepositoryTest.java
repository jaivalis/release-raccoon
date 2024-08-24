package com.raccoon.entity.repository;

import com.raccoon.entity.Artist;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import io.quarkus.panache.common.Page;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@QuarkusTest
@WithTestResource(H2DatabaseTestResource.class)
@TestTransaction
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ArtistRepositoryTest {

    @Inject
    ArtistRepository repository;

    @Test
    void findByName_should_returnEmpty_when_nameDoesNotExist() {
        var name = "does not exist";

        assertThat(repository.findByNameOptional(name)).isEmpty();
    }

    @Test
    void findByName_should_returnArtist_when_nameExists() {
        var name = "name";
        var artist = new Artist();
        artist.setName(name);
        repository.persist(artist);

        final var found = repository.findByNameOptional(name);

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(name);
    }

    @Test
    void listArtistsPaginated_should_returnEmpty_when_noArtistsPresent() {
        var artists = repository.listArtistsPaginated(Page.of(0, 10));

        assertThat(artists).isEmpty();
    }

    @Test
    void listArtistsPaginated_should_returnArtists() {
        var name = "name";
        var artist = new Artist();
        artist.setName(name);
        repository.persist(artist);

        var artists = repository.listArtistsPaginated(Page.of(0, 10));

        assertThat(artists).hasSize(1);
        assertThat(artists).contains(artist);
    }

}
