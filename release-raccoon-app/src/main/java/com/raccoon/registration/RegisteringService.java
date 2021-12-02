package com.raccoon.registration;

import com.raccoon.entity.User;
import com.raccoon.entity.factory.UserFactory;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.exception.ConflictException;
import com.raccoon.mail.RaccoonMailer;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class RegisteringService {

    UserFactory userFactory;
    UserRepository userRepository;
    RaccoonMailer mailer;

    @Inject
    public RegisteringService(final UserFactory userFactory,
                              final UserRepository userRepository,
                              final RaccoonMailer mailer) {
        this.userFactory = userFactory;
        this.userRepository = userRepository;
        this.mailer = mailer;
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

    /**
     * Fetches the user from the database. Sends welcome email blocking
     * @param email unique user identifier
     * @return user from the database.
     */
    public User completeRegistration(final String email) {
        User user = userFactory.getOrCreateUser(email);

        mailer.sendWelcome(
                user,
                () -> log.info("Welcome sent to {}", user.id),
                () -> log.error("Something went wrong while sending welcome to {}", user.id)
        ).await().indefinitely();

        return user;
    }

}