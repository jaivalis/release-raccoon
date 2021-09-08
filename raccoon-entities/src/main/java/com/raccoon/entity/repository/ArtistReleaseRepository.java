package com.raccoon.entity.repository;

import com.raccoon.entity.ArtistRelease;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class ArtistReleaseRepository implements PanacheRepository<ArtistRelease> {

}
