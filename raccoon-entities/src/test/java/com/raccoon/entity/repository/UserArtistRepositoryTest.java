package com.raccoon.entity.repository;

import com.raccoon.entity.UserArtistStubFactory;
import com.raccoon.entity.factory.ArtistFactory;
import com.raccoon.entity.factory.UserFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import javax.inject.Inject;
import javax.transaction.Transactional;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
class UserArtistRepositoryTest {

    @Inject
    UserArtistRepository repository;

    @Inject
    ArtistFactory artistFactory;
    @Inject
    UserFactory userFactory;

    UserArtistStubFactory stubFactory;

    @BeforeEach
    @Transactional
    void setup() {
        repository.deleteAll();
        stubFactory = new UserArtistStubFactory(repository, userFactory, artistFactory);
    }

    @Test
    @Transactional
    void getUserArtistsWithNewReleaseEmpty() {
        assertTrue(repository.getUserArtistsWithNewRelease().isEmpty());
    }

    @Test
    @Transactional
    void getUserArtistsWithNewRelease() {
        var userArtist1 = stubFactory.stubUserArtist("user1", "artist1");
        var userArtist2 = stubFactory.stubUserArtist("user2", "artist2");
        // set the flag for one of the two entries
        userArtist1.setHasNewRelease(true);
        userArtist2.setHasNewRelease(false);
        repository.persist(userArtist1, userArtist2);

        var userArtists = repository.getUserArtistsWithNewRelease();

        assertEquals(1, userArtists.size());
    }

    @Test
    @Transactional
    void markNewReleaseForArtist() {
        var userArtist1 = stubFactory.stubUserArtist("user1", "artist1");
        var userArtist2 = stubFactory.stubUserArtist("user2", "artist2");
        repository.persist(userArtist1, userArtist2);
        var id = userArtist1.getArtist().id;

        var userArtists = repository.markNewRelease(List.of(id));

        assertEquals(1, userArtists.size());
    }

    @Test
    @Transactional
    void markNewReleaseNoIds() {
        assertTrue(repository.markNewRelease(List.of()).isEmpty());
    }

    @Test
    @Transactional
    void findByUserArtistOptional() {
        var userArtist = stubFactory.stubUserArtist("user1", "artist1");
        repository.persist(userArtist);
        var artistId = userArtist.getArtist().id;
        var userId = userArtist.getUser().id;

        var userArtists = repository.findByUserArtistOptional(userId, artistId);

        assertTrue(userArtists.isPresent());
    }

    @Test
    @Transactional
    void findByUserArtistOptionalEmpty() {
        var userArtists = repository.findByUserArtistOptional(1L, 1L);

        assertTrue(userArtists.isEmpty());
    }

//    @Test
//    @Transactional
//    void findByUserId() {
//        var userArtist1 = stubUserArtist("user1", "artist1");
//        var userArtist2 = stubUserArtist("user2", "artist2");
//        repository.persist(userArtist1, userArtist2);
//        var id = userArtist1.getArtist().id;
//
//        var userArtists = repository.findByUserId(id);
//
//        assertEquals(1, userArtists.size());
//    }

    @Test
    @Transactional
    void findByUserIdEmpty() {
        var userArtists = repository.findByUserId(1);

        assertTrue(userArtists.isEmpty());
    }

    @Test
    @Transactional
    void findByUserIdByWeight() {
        var user1Artist1 = stubFactory.stubUserArtist("user1", "artist1");
        var user1Artist2 = stubFactory.stubUserArtist("user1", "artist2");
        var user2Artist1 = stubFactory.stubUserArtist("user2", "artist1");
        user1Artist1.setWeight(0.60f);
        user1Artist2.setWeight(0.65f);
        user2Artist1.setWeight(0.10f);
        repository.persist(List.of(user1Artist1, user1Artist2, user2Artist1));

        var byWeight = repository.findByUserIdByWeight(user1Artist1.getUser().id);

        assertEquals(2, byWeight.size());
        assertEquals("artist2", byWeight.get(0).getArtist().getName());
        assertEquals("artist1", byWeight.get(1).getArtist().getName());
    }

    @Test
    @Transactional
    void deleteAssociation() {
        var userArtist = stubFactory.stubUserArtist("user1", "artist1");
        var userId = userArtist.getUser().id;
        var artistIdNotExistent = userArtist.getArtist().id;
        assertEquals(1, repository.findAll().stream().count());

        repository.deleteAssociation(userId, artistIdNotExistent);

        assertEquals(0, repository.findAll().stream().count());
    }

}
