package com.raccoon.entity.repository;

import com.raccoon.entity.Artist;

import java.util.List;
import java.util.Optional;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.data.page.PageRequest;
import jakarta.data.page.impl.PageRecord;
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

    public Optional<Artist> findByNameOrMusicbrainzId(String name, String musicbrainzId) {
        return Optional.ofNullable(find("name = ?1 or musicbrainzId = ?2", name, musicbrainzId).firstResult());
    }

    public Optional<Artist> findByNameOrSpotifyUriOptional(String name, String spotifyUri) {
        return Optional.ofNullable(find("name = ?1 or spotifyUri = ?2", name, spotifyUri).firstResult());
    }

    public List<Artist> listArtistsPaginated(Page page) {
        PanacheQuery<Artist> query = findAll(Sort.by("id"));
        query.page(page.index, page.size);
        return query.list();
    }

    public jakarta.data.page.Page<Artist> distinctArtistsNotFollowedByUser(PageRequest page, Long userId) {
        var artistsFollowedByUser = find("SELECT DISTINCT ua.key.artist.id FROM UserArtist ua WHERE ua.key.raccoonUser.id = ?1", userId)
                .list();

        PanacheQuery<Artist> query;
        if (artistsFollowedByUser.isEmpty()) {
            query = find("SELECT DISTINCT ua.key.artist FROM UserArtist ua WHERE ua.key.raccoonUser.id <> ?1", userId);
        } else {
            query = find("SELECT DISTINCT ua.key.artist FROM UserArtist ua WHERE ua.key.raccoonUser.id <> ?1 and ua.key.artist.id not in ?2", userId, artistsFollowedByUser);
        }

        List<Artist> artists = query.withHint("org.hibernate.cacheable", Boolean.FALSE)
                // PageRequest is 1 indexed
                .page(Page.of(Long.valueOf(page.page()).intValue() - 1, page.size()))
                .list();

        long totalCount = query.count();

        return new PageRecord<>(page, artists, totalCount);
    }

}
