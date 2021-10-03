package com.raccoon.notify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotifyingResourceTest {

    NotifyingResource resource;

    @Mock
    NotifyService mockService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        resource = new NotifyingResource(mockService);
    }

    @Test
    void notifyUsers() {
        resource.notifyUsers();

        verify(mockService, times(1)).notifyUsers();
    }
}