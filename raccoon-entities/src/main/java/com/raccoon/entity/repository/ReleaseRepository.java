package com.raccoon.entity.repository;

import com.raccoon.entity.Artist;
import com.raccoon.entity.Release;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class ReleaseRepository implements PanacheRepository<Release> {

    public Optional<Release> findBySpotifyUriOptional(String uri) {
        return Optional.ofNullable(find("spotifyUri", uri).firstResult());
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
}
