package com.raccoon.entity.factory;

import com.raccoon.entity.User;

import java.util.Optional;

import static io.quarkus.hibernate.orm.panache.PanacheEntityBase.persist;

public class UserFactory {

    private UserFactory() {}

    /**
     * Creates a User if it is not found in the database, or returns already existing artist.
     * @param email
     * @return
     */
    public static User getOrCreateUser(final String email) {
        Optional<User> existing = User.findByEmailOptional(email);
        if (existing.isPresent()) {
            return existing.get();
        }
        User user = new User();
        user.setEmail(email);
        persist(user);
        return user;
    }
}
