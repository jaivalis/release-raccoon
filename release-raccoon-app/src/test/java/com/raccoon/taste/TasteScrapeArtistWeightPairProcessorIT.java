package com.raccoon.taste;

import com.github.database.rider.cdi.api.DBRider;
import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.raccoon.common.ElasticSearchTestResource;
import com.raccoon.entity.Artist;
import com.raccoon.entity.User;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.repository.ArtistRepository;
import com.raccoon.entity.repository.UserRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.transaction.Transactional;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
@DBRider
@DBUnit(caseSensitiveTableNames = true)
@QuarkusTestResource(ElasticSearchTestResource.class)
class TasteScrapeArtistWeightPairProcessorIT {

    @Inject
    TasteScrapeArtistWeightPairProcessor processor;

    @Inject
    ArtistRepository artistRepository;
    @Inject
    UserRepository userRepository;

    @Test
    @DisplayName("Artist present in db, should be added to the userArtistsSet")
    @DataSet(value = "datasets/yml/notify.yml")
    void testArtistExisted() {
        User user = userRepository.findById(100L);
        Artist existingArtist = artistRepository.findById(100L);
        Set<UserArtist> userArtists = new HashSet<>();

        UserArtist userArtist = processor.delegateProcessArtistWeightPair(user, existingArtist, 0.9F, userArtists);

        assertEquals(1, userArtists.size());
        assertEquals(user, userArtists.iterator().next().getUser());
        assertEquals(existingArtist, userArtists.iterator().next().getArtist());
        assertEquals(100, userArtist.getKey().getUser().id);
        assertEquals(100, userArtist.getKey().getArtist().id);
    }

    @Test
    @DisplayName("Artist not present in db, should not be added to the userArtistsSet")
    @DataSet(value = "datasets/yml/notify.yml")
    void testArtistNotExisted() {
        User user = userRepository.findById(100L);
        Artist newArtist = new Artist();
        newArtist.setName("new artist");
        Set<UserArtist> userArtists = new HashSet<>();

        UserArtist userArtist = processor.delegateProcessArtistWeightPair(user, newArtist, 0.9F, userArtists);

        assertEquals(0, userArtists.size());
        assertEquals(100, userArtist.getKey().getUser().id);
        // Artist was newly created
        assertNotEquals(100, userArtist.getKey().getArtist().id);
        assertNotEquals(101, userArtist.getKey().getArtist().id);
    }

    @Test
    @DisplayName("UserArtist association present in db, should update weight")
    @DataSet(value = "datasets/yml/notify.yml")
    void testUserArtistAssociationExists() {
        User user = userRepository.findById(100L);
        Artist artist = artistRepository.findById(101L);
        Set<UserArtist> userArtists = new HashSet<>();

        UserArtist userArtist = processor.delegateProcessArtistWeightPair(user, artist, 0.9F, userArtists);

        assertEquals(1, userArtists.size());
        assertEquals(100, userArtist.getKey().getUser().id);
        assertEquals(101, userArtist.getKey().getArtist().id);
        assertEquals(0.9F, userArtist.getWeight());
    }
}