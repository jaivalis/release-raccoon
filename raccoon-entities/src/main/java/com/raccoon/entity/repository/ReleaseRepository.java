package com.raccoon.entity.repository;

import com.raccoon.entity.Artist;
import com.raccoon.entity.Release;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class ReleaseRepository implements PanacheRepository<Release> {

    public Optional<Release> findByNameOptional(String name) {
        return Optional.ofNullable(find("name", name).firstResult());
    }

    public Optional<Release> findBySpotifyUriOptional(String uri) {
        return Optional.ofNullable(find("spotifyUri", uri).firstResult());
    }

    public List<Release> findByArtistsSinceDays(Collection<Artist> artists, int days) {
        LocalDate leastDate = LocalDate.now().minusDays(days);

        return Release.list("releasedOn > ?1", leastDate)
                .stream()
                .map(Release.class::cast)
                .filter(release -> release.getArtists().stream().anyMatch(artists::contains))
                .collect(Collectors.toList());
    }
}
