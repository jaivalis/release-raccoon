package com.raccoon.entity.factory;

import com.raccoon.entity.Artist;
import com.raccoon.entity.repository.ArtistRepository;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import static io.quarkus.hibernate.orm.panache.PanacheEntityBase.persist;

@ApplicationScoped
public class ArtistFactory {

    ArtistRepository artistRepository;

    /**
     * Creates an Artist if it is not found in the database, or returns already existing artist.
     * @param name
     * @return
     */
    public Artist getOrCreateArtist(final String name) {
        Optional<Artist> existing = artistRepository.findByNameOptional(name);

        if (existing.isEmpty()) {
            var artist = new Artist();
            artist.setName(name);
            persist(artist);
            return artist;
        }
        return existing.get();
    }
}
