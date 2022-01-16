package com.raccoon.entity.repository;

import com.raccoon.entity.Artist;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class ArtistRepository implements PanacheRepository<Artist> {

    public Optional<Artist> findByNameOptional(String name) {
        return Optional.ofNullable(find("name", name).firstResult());
    }

    public Optional<Artist> findByIdAndNameOptional(Long id, String name) {
        return find("(artistId = ?1 and name = ?2)", id, name).stream().findFirst();
    }

}
