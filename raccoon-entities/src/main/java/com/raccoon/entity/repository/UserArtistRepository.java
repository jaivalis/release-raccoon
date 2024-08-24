package com.raccoon.entity.repository;

import com.raccoon.entity.UserArtist;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class UserArtistRepository implements PanacheRepository<UserArtist> {

    public List<UserArtist> getUserArtistsWithNewRelease() {
        Stream<UserArtist> stream = find("hasNewRelease", true)
                .stream();
        return stream.toList();
    }

    /**
     * For a given collection of artistIds find all UserArtist entries that are referred (by `artist_id`) and set
     * `hasNewRelease` to true.
     * @param artistIds Collection of artistIds.
     * @return a list of updated UserArtist entries.
     */
    public List<UserArtist> markNewRelease(final Collection<Long> artistIds) {
        List<UserArtist> collect = findByArtistIds(artistIds);
        collect.forEach(ua -> {
            ua.setHasNewRelease(Boolean.TRUE);
            ua.persist();
        });
        return collect;
    }

    public Optional<UserArtist> findByUserIdArtistIdOptional(final Long userId, final Long artistId) {
        Objects.requireNonNull(userId);
        Objects.requireNonNull(artistId);
        return find("(key.raccoonUser.id = ?1 and key.artist.id = ?2)", userId, artistId).stream().findAny();
    }

    public List<UserArtist> findByUserId(final long userId) {
        return find("key.raccoonUser.id = ?1", userId).stream().toList();
    }

    public List<UserArtist> findByUserIdSortedByWeight(final long userId) {
        return find("key.raccoonUser.id = ?1", Sort.by("weight", Sort.Direction.Descending), userId)
                .stream().toList();
    }

    public List<UserArtist> findByArtistIds(final Collection<Long> artistIds) {
        return find("key.artist.id in ?1", artistIds).stream().toList();
    }

    // Could be simplified further if it returned only a list of ids, since that's the only way it's
    // used right now.
    public List<UserArtist> findByUserIdAndArtistIds(final Long userId, final Collection<Long> artistIds) {
        return find("key.raccoonUser.id = ?1 and key.artist.id in ?2", userId, artistIds).stream().toList();
    }

    public void deleteAssociation(Long userId, Long artistId) {
        delete("key.raccoonUser.id = ?1 and key.artist.id = ?2", userId, artistId);
    }

}
