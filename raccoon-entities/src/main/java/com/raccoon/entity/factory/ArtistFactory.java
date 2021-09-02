package com.raccoon.entity.factory;

import com.raccoon.entity.Artist;
import com.raccoon.entity.repository.ArtistRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.validation.constraints.NotNull;

import static io.quarkus.hibernate.orm.panache.PanacheEntityBase.persist;

@ApplicationScoped
public class ArtistFactory {

    ArtistRepository artistRepository;

    public ArtistFactory(final ArtistRepository artistRepository) {
        this.artistRepository = artistRepository;
    }

    /**
     * Creates an Artist if it is not found in the database, or returns already existing artist.
     * @param name
     * @return
     */
    public Artist getOrCreateArtist(@NotNull final String name) {
        var existing = artistRepository.findByNameOptional(name);

        if (existing.isEmpty()) {
            var artist = new Artist();
            artist.setName(name);
            persist(artist);
            return artist;
        }
        return existing.get();
    }
}
