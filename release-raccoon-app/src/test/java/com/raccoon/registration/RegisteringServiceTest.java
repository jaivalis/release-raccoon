package com.raccoon.registration;

import com.raccoon.entity.User;
import com.raccoon.entity.factory.UserFactory;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.exception.ConflictException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RegisteringServiceTest {

    @Inject
    RegisteringService service;

    @Mock
    UserRepository userRepositoryMock;
    @Mock
    UserFactory userFactoryMock;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        service = new RegisteringService(userFactoryMock, userRepositoryMock);
    }

    @Test
    @DisplayName("Existing user registration by email should return 409")
    void registrationConflict() {
        Mockito.when(userRepositoryMock.findByEmailOptional("user@gmail.com")).thenReturn(Optional.of(new User()));

        assertThrows(ConflictException.class,
                () -> service.registerUser("name", "user@gmail.com", "lastfm", false));
    }

    @Test
    @DisplayName("successful registration")
    void registrationSuccess() {
        var email = "user@mail.com";
        var userStub = new User();
        userStub.setEmail(email);
        Mockito.when(userRepositoryMock.findByEmailOptional(email)).thenReturn(Optional.empty());
        Mockito.when(userFactoryMock.getOrCreateUser(email)).thenReturn(userStub);

        final var user = service.registerUser("name", email, "lastfm", false);

        assertEquals(userStub.getEmail(), user.getEmail());
        assertEquals("lastfm", userStub.getLastfmUsername());
        assertEquals(false, userStub.getSpotifyEnabled());
        verify(userRepositoryMock, times(1)).persist(user);
    }

    @Test
    @DisplayName("user not found, returns 404")
    void enableSourceNotExistent() {
        var email = "user@mail.com";
        Mockito.when(userRepositoryMock.findByEmailOptional(email)).thenReturn(Optional.empty());

        var arg1 = Optional.of("lastfm");
        Optional<Boolean> arg2 = Optional.empty();
        assertThrows(NotFoundException.class,
                () -> service.enableTasteSources(email, arg1, arg2));
    }

    @Test
    void enableLastfm() {
        var email = "user@mail.com";
        var userStub = new User();
        userStub.setEmail(email);
        Mockito.when(userRepositoryMock.findByEmailOptional(email)).thenReturn(Optional.of(userStub));

        final var user = service.enableTasteSources(email, Optional.of("lastfm"), Optional.empty());

        assertEquals("lastfm", user.getLastfmUsername());
        assertNull(user.getSpotifyEnabled());
    }

    @Test
    void enableSpotify() {
        var email = "user@mail.com";
        var userStub = new User();
        userStub.setEmail(email);
        Mockito.when(userRepositoryMock.findByEmailOptional(email)).thenReturn(Optional.of(userStub));

        final var user = service.enableTasteSources(email, Optional.empty(), Optional.of(Boolean.TRUE));

        assertNull(user.getLastfmUsername());
        assertEquals(Boolean.TRUE, user.getSpotifyEnabled());
    }
}