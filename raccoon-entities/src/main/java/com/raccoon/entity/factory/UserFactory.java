package com.raccoon.entity.factory;

import com.raccoon.entity.User;
import com.raccoon.entity.repository.UserRepository;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserFactory {

    UserRepository userRepository;

    public UserFactory(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Creates a User if it is not found in the database, or returns already existing user.
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
        userRepository.persist(user);
        return user;
    }
}
