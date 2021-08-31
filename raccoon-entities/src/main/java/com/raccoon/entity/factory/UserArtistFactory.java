package com.raccoon.entity.factory;

import com.raccoon.entity.Artist;
import com.raccoon.entity.User;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.repository.ArtistRepository;
import com.raccoon.entity.repository.UserArtistRepository;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserArtistFactory {

    ArtistRepository artistRepository;
    UserArtistRepository userArtistRepository;

    UserArtistFactory(final ArtistRepository artistRepository,
                      final UserArtistRepository repository) {
        this.artistRepository = artistRepository;
        this.userArtistRepository = repository;
    }

    /**
     * Creates an Artist if it is not found in the database, or returns already existing artist.
     * @param user
     * @param artist
     * @return
     */
    public UserArtist getOrCreateUserArtist(final User user,
                                            final Artist artist) {
        Optional<UserArtist> existing = (user.id != null && artist.id != null) ?
                userArtistRepository.findByUserArtistOptional(user.id, artist.id)
                : Optional.empty();
        if (existing.isEmpty()) {
            var userArtist = new UserArtist();
            userArtist.setArtist(artist);
            userArtist.setUser(user);
            artistRepository.persist(artist);
            return userArtist;
        }
        return existing.get();
    }
}
