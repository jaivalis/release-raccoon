package com.raccoon.entity.integration;

import com.raccoon.entity.UserArtistStubFactory;
import com.raccoon.entity.factory.ArtistFactory;
import com.raccoon.entity.factory.UserFactory;
import com.raccoon.entity.repository.ArtistRepository;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.entity.repository.UserRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.util.List;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@WithTestResource(H2DatabaseTestResource.class)
@TestTransaction
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class RaccoonUserArtistRepositoryIT {

    @Inject
    UserArtistRepository userArtistRepository;
    @Inject
    UserRepository userRepository;
    @Inject
    ArtistRepository artistRepository;

    @Inject
    ArtistFactory artistFactory;
    @Inject
    UserFactory userFactory;

    UserArtistStubFactory stubFactory;

    @BeforeEach
    void setup() {
        stubFactory = new UserArtistStubFactory(userArtistRepository, userFactory, userRepository, artistFactory, artistRepository);
    }

    @AfterEach
    void tearDown() {
        userArtistRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void getUserArtistsWithNewReleaseEmpty() {
        assertTrue(userArtistRepository.getUserArtistsWithNewRelease().isEmpty());
    }

    @Test
    void getUserArtistsWithNewRelease() {
        var userArtist1 = stubFactory.stubUserArtist("user1", "artist1");
        var userArtist2 = stubFactory.stubUserArtist("user2", "artist2");
        // set the flag for one of the two entries
        userArtist1.hasNewRelease = true;
        userArtist2.hasNewRelease = false;
        userArtistRepository.persist(userArtist1, userArtist2);

        var userArtists = userArtistRepository.getUserArtistsWithNewRelease();

        assertEquals(1, userArtists.size());
    }

    @Test
    void markNewReleaseForArtist() {
        var userArtist1 = stubFactory.stubUserArtist("user1", "artist1");
        var userArtist2 = stubFactory.stubUserArtist("user2", "artist2");
        userArtistRepository.persist(userArtist1, userArtist2);
        var id = userArtist1.getArtist().id;

        var userArtists = userArtistRepository.markNewRelease(List.of(id));

        assertEquals(1, userArtists.size());
    }

    @Test
    void markNewRelease_should_returnTrue_when_artistIdsEmpty() {
        assertTrue(userArtistRepository.markNewRelease(List.of()).isEmpty());
    }

    @Test
    void findByUserArtistOptional() {
        var userArtist = stubFactory.stubUserArtist("user1", "artist1");
        userArtistRepository.persist(userArtist);
        var artistId = userArtist.getArtist().id;
        var userId = userArtist.getUser().id;

        var userArtists = userArtistRepository.findByUserIdArtistIdOptional(userId, artistId);

        assertTrue(userArtists.isPresent());
    }

    @Test
    void findByUserArtistOptionalEmpty() {
        var userArtists = userArtistRepository.findByUserIdArtistIdOptional(1L, 1L);

        assertTrue(userArtists.isEmpty());
    }

    @Test
    void findByUserId() {
        var userArtist1 = stubFactory.stubUserArtist("user1", "artist1");
        var userArtist2 = stubFactory.stubUserArtist("user2", "artist2");
        userArtistRepository.persist(userArtist1, userArtist2);
        var id = userArtist1.getUser().id;

        var userArtists = userArtistRepository.findByUserId(id);

        assertEquals(1, userArtists.size());
    }

    @Test
    void findByUserIdEmpty() {
        var userArtists = userArtistRepository.findByUserId(1);

        assertTrue(userArtists.isEmpty());
    }

    @Test
    void findByUserIdByWeight() {
        var user1Artist1 = stubFactory.stubUserArtist("user1", "artist1");
        var user1Artist2 = stubFactory.stubUserArtist("user1", "artist2");
        var user2Artist1 = stubFactory.stubUserArtist("user2", "artist1");
        user1Artist1.weight = 0.60f;
        user1Artist2.weight = 0.65f;
        user2Artist1.weight = 0.10f;
        userArtistRepository.persist(List.of(user1Artist1, user1Artist2, user2Artist1));

        var byWeight = userArtistRepository.findByUserIdSortedByWeight(user1Artist1.getUser().id);

        assertEquals(2, byWeight.size());
        assertEquals("artist2", byWeight.get(0).getArtist().getName());
        assertEquals("artist1", byWeight.get(1).getArtist().getName());
    }

    @Test
    void findByUserIdAndArtistIds() {
        var user1Artist1 = stubFactory.stubUserArtist("user1", "artist1");
        var user1Artist2 = stubFactory.stubUserArtist("user1", "artist2");
        var user2Artist1 = stubFactory.stubUserArtist("user2", "artist1");
        user1Artist1.weight = 0.60f;
        user1Artist2.weight = 0.65f;
        user2Artist1.weight = 0.10f;
        userArtistRepository.persist(List.of(user1Artist1, user1Artist2, user2Artist1));

        var foundArtists = userArtistRepository.findByUserIdAndArtistIds(
                user1Artist1.getUser().id,
                List.of(user1Artist2.getArtist().id, user1Artist1.getArtist().id)
        );

        assertEquals(2, foundArtists.size());
        var foundArtistIds = foundArtists.stream().map(userArtist -> userArtist.getArtist().id).toList();
        assertTrue(foundArtistIds.contains(user1Artist1.getArtist().id));
        assertTrue(foundArtistIds.contains(user1Artist1.getArtist().id));
    }

    @Test
    void deleteAssociation_should_removeFromTable() {
        var userArtist = stubFactory.stubUserArtist("user1", "artist1");
        var userId = userArtist.getUser().id;
        var artistIdNotExistent = userArtist.getArtist().id;
        assertEquals(1, userArtistRepository.findAll().stream().count());

        userArtistRepository.deleteAssociation(userId, artistIdNotExistent);

        assertEquals(0, userArtistRepository.findAll().stream().count());
    }

}
