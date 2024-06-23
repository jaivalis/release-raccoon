package com.raccoon.entity.factory;

import com.raccoon.entity.Artist;
import com.raccoon.entity.repository.ArtistRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.constraints.NotNull;

@ApplicationScoped
public class ArtistFactory {

    ArtistRepository artistRepository;

    public ArtistFactory(final ArtistRepository artistRepository) {
        this.artistRepository = artistRepository;
    }

    /**
     * Creates an Artist if it is not found in the database, or returns already existing artist.
     * The new artist has not been persisted, callers responsibility to do so.
     * @param name
     * @return
     */
    public Artist getOrCreateArtist(@NotNull final String name) {
        var existing = artistRepository.findByNameOptional(name);

        if (existing.isEmpty()) {
            var artist = new Artist();
            artist.setName(name);
            return artist;
        }
        return existing.get();
    }
}
