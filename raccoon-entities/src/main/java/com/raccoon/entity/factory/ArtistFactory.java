package com.raccoon.entity.factory;

import com.raccoon.entity.Artist;

import java.util.Optional;

import static io.quarkus.hibernate.orm.panache.PanacheEntityBase.persist;

public class ArtistFactory {

    private ArtistFactory() {}

    /**
     * Creates an Artist if it is not found in the database, or returns already existing artist.
     * @param name
     * @return
     */
    public static Artist getOrCreateArtist(final String name) {
        Optional<Artist> existing = Artist.findByNameOptional(name);

        if (existing.isEmpty()) {
            var artist = new Artist();
            artist.setName(name);
            persist(artist);
            return artist;
        }
        return existing.get();
    }
}
