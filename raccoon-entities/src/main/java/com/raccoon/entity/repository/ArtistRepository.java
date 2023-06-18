package com.raccoon.entity.repository;

import com.raccoon.entity.Artist;
import com.raccoon.entity.UserArtist;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
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
        PanacheQuery<Artist> query = Artist.findAll(Sort.by("id"));
        query.page(page.index, page.size);
        return query.list();
    }

    public List<Artist> listDistinctArtistsNotFollowedByUser(Page page, Long userId) {
        var artistsFollowedByUser = find("SELECT DISTINCT ua.key.artist.id FROM UserArtist ua WHERE ua.key.raccoonUser.id = ?1", userId)
                .list();

        PanacheQuery<PanacheEntityBase> query;
        if (artistsFollowedByUser.isEmpty()) {
            query = UserArtist.find("SELECT DISTINCT ua.key.artist FROM UserArtist ua WHERE ua.key.raccoonUser.id <> ?1", userId);
        } else {
            query = UserArtist.find("SELECT DISTINCT ua.key.artist FROM UserArtist ua WHERE ua.key.raccoonUser.id <> ?1 and ua.key.artist.id not in ?2", userId, artistsFollowedByUser);
        }

        return query
                .withHint("org.hibernate.cacheable", Boolean.FALSE)
                .page(Page.of(page.index, page.size))
                .list();
    }

}
