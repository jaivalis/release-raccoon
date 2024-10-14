package com.raccoon.entity.repository;

import com.raccoon.entity.UserSettings;

import java.util.Optional;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserSettingsRepository implements PanacheRepository<UserSettings> {

    public Optional<UserSettings> findByUserId(Long userId) {
        return find("user.id", userId).stream().findFirst();
    }

}
