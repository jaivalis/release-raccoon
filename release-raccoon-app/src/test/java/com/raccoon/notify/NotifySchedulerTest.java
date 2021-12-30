package com.raccoon.notify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotifySchedulerTest {

    NotifyScheduler scheduler;

    @Mock
    NotifyService mockService;

    @BeforeEach
    void setUp() {
        scheduler = new NotifyScheduler(mockService);
    }

    @Test
    @DisplayName("notifyCronJob() calls mockService.notifyUsers()")
    void notifyCronJob() {
        scheduler.notifyCronJob();

        verify(mockService, times(1)).notifyUsers();
    }

}