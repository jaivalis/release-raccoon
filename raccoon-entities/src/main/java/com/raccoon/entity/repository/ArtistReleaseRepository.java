package com.raccoon.entity.repository;

import com.raccoon.entity.ArtistRelease;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ArtistReleaseRepository implements PanacheRepository<ArtistRelease> {

}
