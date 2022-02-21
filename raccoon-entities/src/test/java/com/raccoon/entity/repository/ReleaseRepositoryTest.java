package com.raccoon.entity.repository;

import com.raccoon.common.ElasticSearchTestResource;
import com.raccoon.entity.Artist;
import com.raccoon.entity.ArtistRelease;
import com.raccoon.entity.Release;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;

import static org.assertj.core.api.Assertions.assertThat;
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
    @DisplayName("A Release with not the same name but matched Artists, should return empty")
    @TestTransaction
    void findByNameAndArtistsOptionalNameNotMatched() {
        var sharedReleaseName = "Shared release name";
        // persist artists
        var artist1 = new Artist();
        artist1.setName("name1");
        var artist2 = new Artist();
        artist2.setName("name2");
        artistRepository.persist(List.of(artist1, artist2));
        // persist release with the same name but two artists
        var release = new Release();
        release.setName(sharedReleaseName);
        repository.persist(List.of(release));
        // Create artist1Release1 associations
        var artist1Release1Association = new ArtistRelease();
        artist1Release1Association.setArtist(artist1);
        artist1Release1Association.setRelease(release);
        artist1Release1Association.setArtist(artist2);
        artist1Release1Association.setRelease(release);
        release.setReleases(List.of(artist1Release1Association));

        assertThat(
                repository.findByNameAndArtistsOptional("Not " + sharedReleaseName, Set.of(artist1, artist2))
        ).as("Find matching Artists but not matched name should yield no Releases").isEmpty();
    }

    @Test
    @DisplayName("Two releases with same name but from different artists")
    @TestTransaction
    void findByNameAndArtistsOptionalTwoReleases() {
        var sharedReleaseName = "Shared release name";
        // persist artists
        var artist1 = new Artist();
        artist1.setName("name1");
        var artist2 = new Artist();
        artist2.setName("name2");
        artistRepository.persist(List.of(artist1, artist2));
        // persist two releases with the same name but by different artists
        var release1 = new Release();
        release1.setName(sharedReleaseName);
        var release2 = new Release();
        release2.setName(sharedReleaseName);
        repository.persist(List.of(release1, release2));
        // Create artist1Release1, artist2Release2 associations
        var artist1Release1Association = new ArtistRelease();
        artist1Release1Association.setArtist(artist1);
        artist1Release1Association.setRelease(release1);
        release1.setReleases(List.of(artist1Release1Association));
        var artist2Release2Association = new ArtistRelease();
        artist2Release2Association.setArtist(artist2);
        artist2Release2Association.setRelease(release2);
        release2.setReleases(List.of(artist2Release2Association));

        assertThat(
                repository.findByNameAndArtistsOptional(sharedReleaseName, Set.of(artist1))
        ).isPresent().hasValue(release1);
    }

    @Test
    @DisplayName("A Release with same name but more than the requested artist, should return empty")
    @TestTransaction
    void findByNameAndArtistsOptionalNotAllArtistsInQuery() {
        var sharedReleaseName = "Shared release name";
        // persist artists
        var artist1 = new Artist();
        artist1.setName("name1");
        var artist2 = new Artist();
        artist2.setName("name2");
        artistRepository.persist(List.of(artist1, artist2));
        // persist release with the same name but two artists
        var release = new Release();
        release.setName(sharedReleaseName);
        repository.persist(List.of(release));
        // Create artist1Release1 associations
        var artist1Release1Association = new ArtistRelease();
        artist1Release1Association.setArtist(artist1);
        artist1Release1Association.setRelease(release);
        artist1Release1Association.setArtist(artist2);
        artist1Release1Association.setRelease(release);
        release.setReleases(List.of(artist1Release1Association));

        assertThat(
                repository.findByNameAndArtistsOptional(sharedReleaseName, Set.of(artist1))
        ).as("Find match on name and only a single Artist should yield no Releases").isEmpty();
    }


    @Test
    @TestTransaction
    void findByArtistsSinceDaysEmpty() {
        var artists = List.of(new Artist());

        assertTrue(repository.findByArtistsSinceDays(artists, 100).isEmpty());
    }

    @Test
    @TestTransaction
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
    @TestTransaction
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

    @Test
    @TestTransaction
    void findByMusicbrainzIdOptional() {
        var id = "id";
        var release = new Release();
        release.setMusicbrainzId(id);
        repository.persist(List.of(release));

        final var found = repository.findByMusicbrainzIdOptional(id);

        assertThat(found).isPresent();
    }

    @Test
    @TestTransaction
    void findByMusicbrainzIdOptionalEmpty() {
        final var found = repository.findByMusicbrainzIdOptional("notFound");

        assertThat(found).isEmpty();
    }

    @Test
    @TestTransaction
    void findBySpotifyUriOptional() {
        var id = "id";
        var release = new Release();
        release.setSpotifyUri(id);
        repository.persist(List.of(release));

        final var found = repository.findBySpotifyUriOptional(id);

        assertThat(found).isPresent();
    }

    @Test
    @TestTransaction
    void findBySpotifyUriOptionalEmpty() {
        final var found = repository.findBySpotifyUriOptional("notFound");

        assertThat(found).isEmpty();
    }

}
