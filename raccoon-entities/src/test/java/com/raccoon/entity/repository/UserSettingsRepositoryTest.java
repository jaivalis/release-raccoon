package com.raccoon.entity.repository;

import com.raccoon.entity.RaccoonUser;
import com.raccoon.entity.UserSettings;
import com.raccoon.entity.factory.UserFactory;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@WithTestResource(H2DatabaseTestResource.class)
@TestTransaction
class UserSettingsRepositoryTest {

    @Inject
    UserRepository userRepository;
    @Inject
    UserSettingsRepository userSettingsRepository;
    @Inject
    UserFactory factory;

    @Test
    void findByUserId_should_returnUserSettings_when_userExists() {
        RaccoonUser user = factory.createUser("email@mail.com");
        UserSettings settings = new UserSettings();
        settings.setUser(user);
        userSettingsRepository.persist(settings);

        Optional<UserSettings> result = userSettingsRepository.findByUserId(user.id);

        assertThat(result).isPresent();
        assertThat(result.get().getUser())
                .isEqualTo(user);
    }

    @Test
    void findByUserId_should_returnEmpty_when_userDoesNotExist() {
        RaccoonUser user = factory.createUser("email@mail.com");
        userRepository.persist(List.of(user));
        UserSettings settings = new UserSettings();
        settings.setUser(user);
        userSettingsRepository.persist(settings);

        Optional<UserSettings> result = userSettingsRepository.findByUserId(user.id + 1);

        assertThat(result).isNotPresent();
    }
}