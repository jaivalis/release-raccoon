package com.raccoon.entity.repository;

import com.raccoon.entity.Artist;

import java.util.List;
import java.util.Optional;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class ArtistRepository implements PanacheRepository<Artist> {

    EntityManager entityManager;

    @Inject
    public ArtistRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Optional<Artist> findByNameOptional(String name) {
        return Optional.ofNullable(find("name", name).firstResult());
    }

    public List<Artist> listArtistsPaginated(Page page) {
        PanacheQuery<Artist> query = findAll(Sort.by("id"));
        query.page(page.index, page.size);
        return query.list();
    }

    public List<Artist> listDistinctArtistsNotFollowedByUser(Page page, Long userId) {
        var artistsFollowedByUser = find("SELECT DISTINCT ua.key.artist.id FROM UserArtist ua WHERE ua.key.raccoonUser.id = ?1", userId)
                .list();

        PanacheQuery<Artist> query;
        if (artistsFollowedByUser.isEmpty()) {
            query = find("SELECT DISTINCT ua.key.artist FROM UserArtist ua WHERE ua.key.raccoonUser.id <> ?1", userId);
        } else {
            query = find("SELECT DISTINCT ua.key.artist FROM UserArtist ua WHERE ua.key.raccoonUser.id <> ?1 and ua.key.artist.id not in ?2", userId, artistsFollowedByUser);
        }

        return query
                .withHint("org.hibernate.cacheable", Boolean.FALSE)
                .page(Page.of(page.index, page.size))
                .list();
    }

}
