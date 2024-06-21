package com.raccoon.integration.taste;

import com.raccoon.entity.Artist;
import com.raccoon.entity.RaccoonUser;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.repository.ArtistRepository;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.integration.profile.TasteScrapeDatabaseProfile;
import com.raccoon.taste.TasteScrapeArtistWeightPairProcessor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestTransaction
@TestProfile(value = TasteScrapeDatabaseProfile.class)
class TasteScrapeArtistWeightPairProcessorIT {

    @Inject
    TasteScrapeArtistWeightPairProcessor processor;

    @Inject
    ArtistRepository artistRepository;
    @Inject
    UserRepository userRepository;

    @Test
    @DisplayName("Artist present in db, should be added to the userArtistsSet")
    void testArtistExisted() {
        RaccoonUser raccoonUser = userRepository.findById(500L);
        Artist existingArtist = artistRepository.findById(500L);
        Set<UserArtist> userArtists = new HashSet<>();

        UserArtist userArtist = processor.delegateProcessArtistWeightPair(raccoonUser, existingArtist, 0.9F, userArtists);

        assertThat(userArtists)
                .hasSize(1);
        assertThat(userArtists.iterator().next().getUser())
                .isEqualTo(raccoonUser);
        // Artist was newly created
        assertThat(userArtists.iterator().next().getArtist())
                .isEqualTo(existingArtist);
        assertThat(userArtist.key.getArtist().id)
                .isEqualTo(500);
    }

    @Test
    @DisplayName("Artist not present in db, should not be added to the userArtistsSet")
    void testArtistNotExisted() {
        RaccoonUser raccoonUser = userRepository.findById(500L);
        Artist newArtist = new Artist();
        newArtist.setName("new artist");
        Set<UserArtist> userArtists = new HashSet<>();

        UserArtist userArtist = processor.delegateProcessArtistWeightPair(raccoonUser, newArtist, 0.9F, userArtists);

        assertThat(userArtists)
                .isEmpty();
        assertThat(userArtist.key.getRaccoonUser().id)
                .isEqualTo(500);
        // Artist was newly created
        assertThat(userArtist.key.getArtist().id)
                .isNotEqualTo(500);
        assertThat(userArtist.key.getArtist().id)
                .isNotEqualTo(501);
    }

    @Test
    @DisplayName("UserArtist association present in db, should update weight")
    void testUserArtistAssociationExists() {
        RaccoonUser raccoonUser = userRepository.findById(500L);
        Artist artist = artistRepository.findById(501L);
        Set<UserArtist> userArtists = new HashSet<>();

        UserArtist userArtist = processor.delegateProcessArtistWeightPair(raccoonUser, artist, 0.9F, userArtists);

        assertThat(userArtists)
                .hasSize(1);
        assertThat(userArtist.key.getRaccoonUser().id)
                .isEqualTo(500);
        assertThat(userArtist.key.getArtist().id)
                .isEqualTo(501);
        assertThat(userArtist.weight)
                .isEqualTo(0.9F);
    }

}
