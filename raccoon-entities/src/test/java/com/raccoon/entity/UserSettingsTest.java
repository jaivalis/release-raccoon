package com.raccoon.entity;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class UserSettingsTest {

    @Test
    public void shouldNotify_should_returnTrue_when_lastNotifiedNull() {
        UserSettings userSettings = new UserSettings();
        userSettings.setNotifyIntervalDays(1);
        userSettings.setUnsubscribed(false);

        assertThat(userSettings.shouldNotify(null)).isTrue();
    }

    @Test
    public void shouldNotify_should_returnTrue_when_withinInterval() {
        UserSettings userSettings = new UserSettings();
        userSettings.setNotifyIntervalDays(1);
        userSettings.setUnsubscribed(false);
        LocalDate lastNotified = LocalDate.now().minusDays(2);

        assertThat(userSettings.shouldNotify(lastNotified)).isTrue();
    }

    @Test
    public void shouldNotify_should_returnTrue_when_outsideInterval() {
        UserSettings userSettings = new UserSettings();
        userSettings.setNotifyIntervalDays(1);
        userSettings.setUnsubscribed(false);
        LocalDate lastNotified = LocalDate.now();

        assertThat(userSettings.shouldNotify(lastNotified))
                .isFalse();
    }

    @Test
    public void shouldNotify_should_returnTrue_when_outsideInterval2() {
        UserSettings userSettings = new UserSettings();
        userSettings.setNotifyIntervalDays(3);
        userSettings.setUnsubscribed(false);
        LocalDate lastNotified = LocalDate.now().minusDays(1);

        boolean result = userSettings.shouldNotify(lastNotified);

        assertThat(result).isFalse();
    }

    @Test
    public void shouldNotify_should_returnFalse_when_unsubscribed() {
        UserSettings userSettings = new UserSettings();
        userSettings.setNotifyIntervalDays(1);
        userSettings.setUnsubscribed(true);
        LocalDate lastNotified = LocalDate.now().minusDays(1);

        boolean result = userSettings.shouldNotify(lastNotified);

        assertThat(result).isFalse();
    }
}