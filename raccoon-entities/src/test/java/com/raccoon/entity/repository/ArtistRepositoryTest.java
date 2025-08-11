package com.raccoon.entity.repository;

import com.raccoon.entity.Artist;
import com.raccoon.entity.RaccoonUser;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.UserArtistPK;

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
    @Inject
    UserRepository userRepository;

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

    @Test
    void getFollowerCount_should_returnZero_when_noFollowers() {
        var artist = new Artist();
        artist.setName("test-artist");
        repository.persist(artist);

        Integer followerCount = repository.getFollowerCount(artist.id);

        assertThat(followerCount).isZero();
    }

    @Test
    void getFollowerCount_should_returnCorrectCount_when_artistHasFollowers() {
        var artist = new Artist();
        artist.setName("test-artist");
        repository.persist(artist);

        var user1 = new RaccoonUser();
        user1.setEmail("user1@test.com");
        userRepository.persist(user1);

        var user2 = new RaccoonUser();
        user2.setEmail("user2@test.com");
        userRepository.persist(user2);

        // Create UserArtist relationships
        var userArtist1 = new UserArtist();
        var key1 = new UserArtistPK();
        key1.setRaccoonUser(user1);
        key1.setArtist(artist);
        userArtist1.setKey(key1);
        userArtist1.persist();

        var userArtist2 = new UserArtist();
        var key2 = new UserArtistPK();
        key2.setRaccoonUser(user2);
        key2.setArtist(artist);
        userArtist2.setKey(key2);
        userArtist2.persist();

        Integer followerCount = repository.getFollowerCount(artist.id);

        assertThat(followerCount).isEqualTo(2);
    }

}
