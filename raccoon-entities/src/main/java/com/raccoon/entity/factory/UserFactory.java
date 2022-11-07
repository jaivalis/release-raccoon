package com.raccoon.entity.factory;

import com.raccoon.entity.RaccoonUser;
import com.raccoon.entity.repository.UserRepository;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserFactory {

    UserRepository userRepository;

    public UserFactory(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public RaccoonUser createUser(final String email) {
        var user = new RaccoonUser();
        user.setEmail(email);
        userRepository.persist(user);
        return user;
    }
}
