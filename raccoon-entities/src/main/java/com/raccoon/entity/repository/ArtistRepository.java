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

}
