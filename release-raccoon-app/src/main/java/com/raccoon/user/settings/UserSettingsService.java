package com.raccoon.user.settings;

import com.raccoon.entity.UserSettings;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.entity.repository.UserSettingsRepository;
import com.raccoon.user.settings.dto.UserSettingsDto;
import com.raccoon.user.settings.dto.UserSettingsMapper;

import java.util.Optional;

import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import static com.raccoon.templatedata.QuteTemplateLoader.USER_SETTINGS_TEMPLATE_ID;

@ApplicationScoped
@Slf4j
public class UserSettingsService {

    UserRepository userRepository;
    UserSettingsRepository userSettingsRepository;
    UserSettingsMapper mapper;
    Template settingsTemplate;

    @Inject
    public UserSettingsService(UserRepository userRepository,
                               UserSettingsRepository userSettingsRepository,
                               UserSettingsMapper mapper,
                               Engine engine) {
        this.userRepository = userRepository;
        this.userSettingsRepository = userSettingsRepository;
        this.mapper = mapper;
        settingsTemplate = engine.getTemplate(USER_SETTINGS_TEMPLATE_ID);
    }

    @Transactional
    public UserSettingsDto getUserSettings(String email) {
        var user = userRepository.findByEmail(email);
        log.info("Retrieving user settings for user {}", user.getId());
        UserSettings settings = userSettingsRepository.findByUserId(user.id)
                .orElse(new UserSettings());
        return mapper.toUserSettingsDto(settings);
    }

    @Transactional
    public void addOrUpdateUserSetting(String email, UserSettings userSettings) {
        var user = userRepository.findByEmail(email);
        log.info("Updating user settings for user {}", user.id);
        Optional<UserSettings> existingOpt =
                userSettingsRepository.findByUserId(user.id);
        if (existingOpt.isPresent()) {
            UserSettings existing = existingOpt.get();
            existing.updateFrom(userSettings);
            userSettingsRepository.persist(existing);
        } else {
            userSettings.setUser(user);
            userSettingsRepository.persist(userSettings);
        }
    }

    public String renderSettingsPage(String email) {
        UserSettingsDto settingsDto = getUserSettings(email);
        return settingsTemplate.data("contents", settingsDto)
                .render();
    }
}