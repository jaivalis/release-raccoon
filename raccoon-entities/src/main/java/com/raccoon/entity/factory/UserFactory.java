package com.raccoon.entity.factory;

import com.raccoon.entity.User;
import com.raccoon.entity.repository.UserRepository;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserFactory {

    UserRepository userRepository;

    public UserFactory(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(final String email) {
        var user = new User();
        user.setEmail(email);
        userRepository.persist(user);
        return user;
    }
}
