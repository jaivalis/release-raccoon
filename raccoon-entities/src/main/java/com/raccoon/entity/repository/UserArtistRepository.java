package com.raccoon.entity.repository;

import com.raccoon.entity.UserArtist;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import static java.util.stream.Collectors.toList;

@ApplicationScoped
public class UserArtistRepository implements PanacheRepository<UserArtist> {

    public List<UserArtist> getUserArtistsWithNewRelease() {
        Stream<UserArtist> stream = find("hasNewRelease", true)
                .stream();

        return stream.collect(toList());
    }

    /**
     * For a given collection of artistIds find all UserArtist entries that are referred (by `artist_id`) and set
     * `hasNewRelease` to true.
     * @param artistIds Collection of artistIds.
     * @return a list of updated UserArtist entries.
     */
    public List<UserArtist> markNewRelease(final Collection<Long> artistIds) {
        List<UserArtist> collect = findByArtistIds(artistIds);
        collect.stream()
                .forEach(ua -> {
                    ua.setHasNewRelease(Boolean.TRUE);
                    ua.persist();
                });
        return collect;
    }

    public Optional<PanacheEntityBase> findByUserArtistOptional(final long userId, final long artistId) {
        return UserArtist.find("(user_id = ?1 and artist_id = ?2) ", userId, artistId).stream().findAny();
    }

    public List<PanacheEntityBase> findByUserId(final long userId) {
        return UserArtist.find("user_id = ?1", userId).stream().collect(toList());
    }

    public List<UserArtist> findByArtistIds(final Collection<Long> artistIds) {
        Stream<UserArtist> stream = UserArtist.find("artist_id in ?1", artistIds).stream();
        return stream.collect(Collectors.toList());
    }

}
