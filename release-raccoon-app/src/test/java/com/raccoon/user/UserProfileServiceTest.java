package com.raccoon.user;

import com.raccoon.entity.User;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.taste.lastfm.LastfmTasteUpdatingService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import javax.ws.rs.NotFoundException;

import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

import static com.raccoon.templatedata.TemplateLoader.PROFILE_TEMPLATE_ID;
import static io.smallrye.common.constraint.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    UserProfileService service;

    @Mock
    UserArtistRepository mockUserArtistRepository;
    @Mock
    UserRepository mockUserRepository;
    @Mock
    LastfmTasteUpdatingService mockLastfmTasteUpdatingService;
    @Mock
    Template mockTemplate;
    @Mock
    Engine mockEngine;
    @Mock
    TemplateInstance templateInstanceMock;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(mockEngine.getTemplate(PROFILE_TEMPLATE_ID)).thenReturn(mockTemplate);

        service = new UserProfileService(
                mockUserRepository, mockUserArtistRepository, mockLastfmTasteUpdatingService, mockEngine
        );
    }

    @Test
    void getUserArtists() {
        var user = new User();
        user.id = 1L;

        service.getUserArtists(user);

        verify(mockUserArtistRepository, times(1)).findByUserIdByWeight(1L);
    }

    @Test
    void getTemplateInstance() {
        var user = new User();
        user.setEmail("some@email.com");
        user.setSpotifyEnabled(true);
        user.setLastfmUsername(null);
        user.id = 1L;
        when(mockUserRepository.findByEmail(any())).thenReturn(user);
        when(mockTemplate.data(anyString(), any())).thenReturn(templateInstanceMock);

        service.getTemplateInstance("some@email.com");

        verify(mockTemplate, times(1))
                .data(anyString(), any());
    }

    @Test
    void unfollowArtist() {
        var user = new User();
        user.id = 1L;
        when(mockUserRepository.findByEmail(any())).thenReturn(user);

        service.unfollowArtist("some@mail.com", 2L);

        verify(mockUserArtistRepository).deleteAssociation(1L, 2L);
    }

    @Test
    @DisplayName("user not found, returns 404")
    void enableSourceNotExistent() {
        var email = "user@mail.com";
        Mockito.when(mockUserRepository.findByEmailOptional(email)).thenReturn(Optional.empty());

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
        Mockito.when(mockUserRepository.findByEmailOptional(email)).thenReturn(Optional.of(userStub));

        final var user = service.enableTasteSources(email, Optional.of("lastfm"), Optional.empty());

        assertEquals("lastfm", user.getLastfmUsername());
        assertFalse(user.getSpotifyEnabled());
    }

    @Test
    void enableSpotify() {
        var email = "user@mail.com";
        var userStub = new User();
        userStub.setEmail(email);
        Mockito.when(mockUserRepository.findByEmailOptional(email)).thenReturn(Optional.of(userStub));

        final var user = service.enableTasteSources(email, Optional.empty(), Optional.of(Boolean.TRUE));

        assertNull(user.getLastfmUsername());
        assertEquals(Boolean.TRUE, user.getSpotifyEnabled());
    }
}