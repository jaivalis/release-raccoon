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

import io.quarkus.panache.common.Page;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@WithTestResource(H2DatabaseTestResource.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@TestTransaction
class ArtistRepositoryIT {

    @Inject
    ArtistRepository artistRepository;

    @Inject
    UserArtistRepository userArtistRepository;
    @Inject
    UserRepository userRepository;

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
        artistRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void listDistinctArtistsNotFollowedByUser_should_returnArtistsFollowedByOthersOnly() {
        var user1Artist1 = stubFactory.stubUserArtist("user1@mail.com", "artist1");
        var user1Artist2 = stubFactory.stubUserArtist("user1@mail.com", "artist2");
        var user2Artist1 = stubFactory.stubUserArtist("user2@mail.com", "artist1");
        userArtistRepository.persist(List.of(user1Artist1, user1Artist2, user2Artist1));

        var foundArtists = artistRepository.listDistinctArtistsNotFollowedByUser(
                Page.of(0, 100),
                user2Artist1.getUser().id
        );

        assertThat(foundArtists)
                .hasSize(1)
                .contains(user1Artist2.getArtist());
    }

    @Test
    void listDistinctArtistsNotFollowedByUser_should_returnEmptyWhenNotFound() {
        var user1Artist1 = stubFactory.stubUserArtist("user1@mail.com", "artist1");
        var user1Artist2 = stubFactory.stubUserArtist("user1@mail.com", "artist2");
        userArtistRepository.persist(List.of(user1Artist1, user1Artist2));

        var foundArtists = artistRepository.listDistinctArtistsNotFollowedByUser(
                Page.of(0, 100),
                user1Artist1.getUser().id
        );

        assertThat(foundArtists)
                .isEmpty();
    }

}
