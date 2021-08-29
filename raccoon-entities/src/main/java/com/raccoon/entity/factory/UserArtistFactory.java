package com.raccoon.entity.factory;

import com.raccoon.entity.Artist;
import com.raccoon.entity.User;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.repository.UserArtistRepository;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import static io.quarkus.hibernate.orm.panache.PanacheEntityBase.persist;

@ApplicationScoped
public class UserArtistFactory {

    UserArtistRepository repository;

    UserArtistFactory(UserArtistRepository repository) {
        this.repository = repository;
    }

    /**
     * Creates an Artist if it is not found in the database, or returns already existing artist.
     * @param user
     * @param artist
     * @return
     */
    public UserArtist getOrCreateUserArtist(final User user,
                                            final Artist artist) {
        Optional<PanacheEntityBase> existing = repository.findByUserArtistOptional(user.id, artist.id);
        if (existing.isEmpty()) {
            var userArtist = new UserArtist();
            userArtist.setArtist(artist);
            userArtist.setUser(user);
            persist(artist);
            return userArtist;
        }
        return (UserArtist) existing.get();
    }
}
