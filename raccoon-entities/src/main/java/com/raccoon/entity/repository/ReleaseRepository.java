package com.raccoon.entity.repository;

import com.raccoon.entity.Artist;
import com.raccoon.entity.Release;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.validation.constraints.NotNull;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class ReleaseRepository implements PanacheRepository<Release> {

    /**
     * Finds a Release based on the name of the Release and the Artists involved.
     * @param name the name of the Release
     * @param artistSet the artists that need to be matched
     * @return Optional<Release> empty if not found.
     */
    public Optional<Release> findByNameAndArtistsOptional(String name, Set<Artist> artistSet) {
        List<Release> releasesByName = find("name = ?1", name).list();
        List<Release> releasesByNameAndArtists = releasesByName.stream()
                .filter(release -> release.getArtists().containsAll(artistSet))
                .toList();
        if (releasesByNameAndArtists.isEmpty()) {
            return Optional.empty();
        } else {
            // Having more than one hits would constitute a database consistency error.
            assert releasesByNameAndArtists.size() <= 1;
            return Optional.of(releasesByName.get(0));
        }
    }

    /**
     * Returns all releases by specific artists, released less than {@code days} amount of days ago
     * @param artists collection of artists to query for
     * @param days amount of days to limit the search by
     * @return list of releases.
     */
    public List<Release> findByArtistsSinceDays(Collection<Artist> artists, int days) {
        LocalDate leastDate = LocalDate.now().minusDays(days);

        return list("releasedOn > ?1", leastDate)
                .stream()
                .map(Release.class::cast)
                .filter(release -> release.getArtists().stream().anyMatch(artists::contains))
                .toList();
    }

    /**
     * First lookup on SpotifyUri else on combination of albumName, releaseArtists
     * @param spotifyUri release spotifyUri
     * @param albumName release name
     * @param releaseArtists release artists
     * @return Optional of Release if exists else empty
     */
    public Optional<Release> findSpotifyRelease(@NotNull String spotifyUri,
                                                String albumName,
                                                Set<Artist> releaseArtists) {
        return findBySpotifyUriOptional(spotifyUri)
                .or(() -> findByNameAndArtistsOptional(albumName, releaseArtists));
    }

    /**
     * Lookup on musicbrainzId, if not found on combination of albumName, releaseArtists
     * @param musicbrainzId release musicbrainzId
     * @param albumName release name
     * @param releaseArtists release artists
     * @return Optional of Release if exists else empty
     */
    public Optional<Release> findMusicbrainzRelease(@NotNull String musicbrainzId,
                                                    String albumName,
                                                    Set<Artist> releaseArtists) {
        return findByMusicbrainzIdOptional(musicbrainzId)
                .or(() -> findByNameAndArtistsOptional(albumName, releaseArtists));
    }

////////////////////////////////////////////////////////////////////////////////////////////////////

    Optional<Release> findByMusicbrainzIdOptional(String musicbrainzId) {
        return Optional.ofNullable(find("musicbrainzId", musicbrainzId).firstResult());
    }

    Optional<Release> findBySpotifyUriOptional(String spotifyUri) {
        return Optional.ofNullable(find("spotifyUri", spotifyUri).firstResult());
    }

}
