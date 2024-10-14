package com.raccoon.user.settings;

import com.raccoon.entity.RaccoonUser;
import com.raccoon.entity.UserSettings;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.entity.repository.UserSettingsRepository;
import com.raccoon.user.settings.dto.UserSettingsDto;
import com.raccoon.user.settings.dto.UserSettingsMapper;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserSettingsServiceTest {

    @Test
    void getUserSettings_should_returnUserSettingsDto_when_userExists() {
        UserRepository userRepository = mock(UserRepository.class);
        UserSettingsRepository userSettingsRepository = mock(UserSettingsRepository.class);
        UserSettingsMapper mapper = mock(UserSettingsMapper.class);
        Engine engine = mock(Engine.class);
        Template template = mock(Template.class);

        RaccoonUser user = new RaccoonUser();
        user.setId(1L);
        user.setEmail("test@example.com");

        UserSettings settings = new UserSettings();
        UserSettingsDto dto = new UserSettingsDto();

        when(userRepository.findByEmail("test@example.com")).thenReturn(user);
        when(userSettingsRepository.findByUserId(1L)).thenReturn(Optional.of(settings));
        when(mapper.toUserSettingsDto(settings)).thenReturn(dto);
        when(engine.getTemplate(Mockito.anyString())).thenReturn(template);

        UserSettingsService service = new UserSettingsService(userRepository, userSettingsRepository, mapper, engine);

        UserSettingsDto result = service.getUserSettings("test@example.com");

        assertThat(result).isEqualTo(dto);
    }

    @Test
    void getUserSettings_should_returnNewUserSettingsDto_when_userSettingsNotFound() {
        UserRepository userRepository = mock(UserRepository.class);
        UserSettingsRepository userSettingsRepository = mock(UserSettingsRepository.class);
        UserSettingsMapper mapper = mock(UserSettingsMapper.class);
        Engine engine = mock(Engine.class);
        Template template = mock(Template.class);

        RaccoonUser user = new RaccoonUser();
        user.setId(1L);
        user.setEmail("test@example.com");

        UserSettings newSettings = new UserSettings();
        UserSettingsDto dto = new UserSettingsDto();

        when(userRepository.findByEmail("test@example.com")).thenReturn(user);
        when(userSettingsRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(mapper.toUserSettingsDto(newSettings)).thenReturn(dto);
        when(engine.getTemplate(Mockito.anyString())).thenReturn(template);

        UserSettingsService service = new UserSettingsService(userRepository, userSettingsRepository, mapper, engine);

        UserSettingsDto result = service.getUserSettings("test@example.com");

        assertThat(result).isEqualTo(dto);
    }

    @Test
    void addOrUpdateUserSetting_should_updateExistingSettings_when_settingsExist() {
        UserRepository userRepository = mock(UserRepository.class);
        UserSettingsRepository userSettingsRepository = mock(UserSettingsRepository.class);
        UserSettingsMapper mapper = mock(UserSettingsMapper.class);
        Engine engine = mock(Engine.class);

        RaccoonUser user = new RaccoonUser();
        user.setId(1L);
        user.setEmail("test@example.com");

        UserSettings existingSettings = mock(UserSettings.class);
        UserSettings newSettings = new UserSettings();
        newSettings.setNotifyIntervalDays(5);

        when(userRepository.findByEmail("test@example.com")).thenReturn(user);
        when(userSettingsRepository.findByUserId(1L)).thenReturn(Optional.of(existingSettings));

        UserSettingsService service = new UserSettingsService(userRepository, userSettingsRepository, mapper, engine);

        service.addOrUpdateUserSetting("test@example.com", newSettings);

        verify(userSettingsRepository).persist(existingSettings);
    }

    @Test
    void addOrUpdateUserSetting_should_addNewSettings_when_settingsDoNotExist() {
        UserRepository userRepository = mock(UserRepository.class);
        UserSettingsRepository userSettingsRepository = mock(UserSettingsRepository.class);
        UserSettingsMapper mapper = mock(UserSettingsMapper.class);
        Engine engine = mock(Engine.class);

        RaccoonUser user = new RaccoonUser();
        user.setId(1L);
        user.setEmail("test@example.com");

        UserSettings newSettings = new UserSettings();
        newSettings.setNotifyIntervalDays(5);

        when(userRepository.findByEmail("test@example.com")).thenReturn(user);
        when(userSettingsRepository.findByUserId(1L)).thenReturn(Optional.empty());

        UserSettingsService service = new UserSettingsService(userRepository, userSettingsRepository, mapper, engine);

        service.addOrUpdateUserSetting("test@example.com", newSettings);

        assertThat(newSettings.getUser()).isEqualTo(user);
        verify(userSettingsRepository).persist(newSettings);
    }

}