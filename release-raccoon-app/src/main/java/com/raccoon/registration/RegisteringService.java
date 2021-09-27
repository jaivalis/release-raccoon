package com.raccoon.registration;

import com.raccoon.entity.User;
import com.raccoon.entity.factory.UserFactory;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.exception.ConflictException;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class RegisteringService {

    @Inject
    UserFactory userFactory;
    @Inject
    UserRepository userRepository;

    public RegisteringService(final UserFactory userFactory,
                              final UserRepository userRepository) {
        this.userFactory = userFactory;
        this.userRepository = userRepository;
    }

    public User registerUser(final String username,
                             final String email,
                             final String lastfmUsername,
                             final Boolean spotifyEnabled) throws ConflictException {
        Optional<User> existing = userRepository.findByEmailOptional(email);
        if (existing.isPresent()) {
            log.info("User with email {} exists.", email);
            throw new ConflictException("User with that email already exists");
        }
        var user = userFactory.getOrCreateUser(email);
        user.setLastfmUsername(lastfmUsername);
        user.setSpotifyEnabled(spotifyEnabled);
        user.setUsername(username);

        userRepository.persist(user);
        return user;
    }

    public User completeRegistration(final String username,
                                     final String email,
                                     final String lastfmUsername,
                                     final Boolean spotifyEnabled) {
        Optional<User> existing = userRepository.findByEmailOptional(email);
        if (existing.isEmpty()) {
            log.info("User with email {} exists.", email);

            var user = userFactory.getOrCreateUser(email);
            user.setLastfmUsername(lastfmUsername);
            user.setSpotifyEnabled(spotifyEnabled);
            user.setUsername(username);
            userRepository.persist(user);
            return user;
        }
        return existing.get();
    }

}