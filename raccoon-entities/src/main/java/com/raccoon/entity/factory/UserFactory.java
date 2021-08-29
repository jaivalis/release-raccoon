package com.raccoon.entity.factory;

import com.raccoon.entity.User;
import com.raccoon.entity.repository.UserRepository;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static io.quarkus.hibernate.orm.panache.PanacheEntityBase.persist;

@ApplicationScoped
public class UserFactory {

    @Inject
    UserRepository userRepository;

    private UserFactory() {}

    /**
     * Creates a User if it is not found in the database, or returns already existing artist.
     * @param email
     * @return
     */
    public User getOrCreateUser(final String email) {
        Optional<User> existing = userRepository.findByEmailOptional(email);
        if (existing.isPresent()) {
            return existing.get();
        }
        var user = new User();
        user.setEmail(email);
        persist(user);
        return user;
    }
}
