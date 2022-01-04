package com.raccoon.entity.repository;

import com.raccoon.entity.Artist;
import com.raccoon.entity.ArtistRelease;
import com.raccoon.entity.Release;
import com.raccoon.entity.resources.ElasticSearchTestResource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
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
@QuarkusTestResource(ElasticSearchTestResource.class)
@Testcontainers
class ReleaseRepositoryTest {

    @Inject
    ReleaseRepository repository;

    @Inject
    ArtistRepository artistRepository;

    @Test
    @Transactional
    void findBySpotifyUriEmpty() {
        var uri = "does not exist";

        assertTrue(repository.findBySpotifyUriOptional(uri).isEmpty());
    }

    @Test
    @Transactional
    void findBySpotifyUriOptional() {
        var uri = "uri";
        var release = new Release();
        release.setSpotifyUri(uri);
        repository.persist(release);

        final var found = repository.findBySpotifyUriOptional(uri);

        assertTrue(found.isPresent());
        assertEquals(uri, found.get().getSpotifyUri());
    }

    @Test
    @Transactional
    void findByArtistsSinceDaysEmpty() {
        var artists = List.of(new Artist());

        assertTrue(repository.findByArtistsSinceDays(artists, 100).isEmpty());
    }

    @Test
    @Transactional
    void findByArtistsSinceDaysReturnsOne() {
        var artistName = "artist";
        var release1Name = "release1";
        var release2Name = "release2";
        var artist = new Artist();
        artist.setName(artistName);
        artistRepository.persist(artist);

        var release1 = new Release();
        release1.setReleasedOn(LocalDate.now().minusDays(3));
        release1.setName(release1Name);
        var artistReleaseAssociation1 = new ArtistRelease();
        artistReleaseAssociation1.setArtist(artist);
        artistReleaseAssociation1.setRelease(release1);
        release1.setReleases(List.of(artistReleaseAssociation1));

        var release2 = new Release();
        release2.setReleasedOn(LocalDate.now().minusDays(5));
        var artistReleaseAssociation2 = new ArtistRelease();
        artistReleaseAssociation2.setArtist(artist);
        artistReleaseAssociation2.setRelease(release1);
        release2.setName(release2Name);

        repository.persist(List.of(release1, release2));

        final var found = repository.findByArtistsSinceDays(List.of(artist), 4);

        assertEquals(1, found.size());
    }

    @Test
    @Transactional
    @DisplayName("filters by date")
    void findByArtistsSinceDaysReturnsNone() {
        var artistName = "artist";
        var releaseName = "release1";
        var artist = new Artist();
        artist.setName(artistName);
        artistRepository.persist(artist);

        var release = new Release();
        release.setReleasedOn(LocalDate.now().minusDays(3));
        release.setName(releaseName);
        var artistReleaseAssociation1 = new ArtistRelease();
        artistReleaseAssociation1.setArtist(artist);
        artistReleaseAssociation1.setRelease(release);
        release.setReleases(List.of(artistReleaseAssociation1));

        repository.persist(List.of(release));

        final var found = repository.findByArtistsSinceDays(List.of(artist), 2);

        assertTrue(found.isEmpty());
    }

}
