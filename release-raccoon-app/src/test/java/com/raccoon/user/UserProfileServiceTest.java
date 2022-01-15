package com.raccoon.user;

import com.raccoon.entity.Artist;
import com.raccoon.entity.User;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.factory.UserFactory;
import com.raccoon.entity.repository.ArtistRepository;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.mail.RaccoonMailer;
import com.raccoon.notify.NotifyService;
import com.raccoon.search.dto.ArtistDto;
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

import static com.raccoon.templatedata.QuteTemplateLoader.PROFILE_TEMPLATE_ID;
import static io.smallrye.common.constraint.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    UserProfileService service;

    @Mock
    UserRepository mockUserRepository;
    @Mock
    ArtistRepository mockArtistRepository;
    @Mock
    UserArtistRepository mockUserArtistRepository;
    @Mock
    UserFactory mockUserFactory;
    @Mock
    LastfmTasteUpdatingService mockLastfmTasteUpdatingService;
    @Mock
    Template mockTemplate;
    @Mock
    RaccoonMailer mockMailer;
    @Mock
    Engine mockEngine;
    @Mock
    TemplateInstance templateInstanceMock;
    @Mock
    NotifyService mockNotifyService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(mockEngine.getTemplate(PROFILE_TEMPLATE_ID)).thenReturn(mockTemplate);

        service = new UserProfileService(
                mockUserRepository, mockArtistRepository, mockUserFactory, mockUserArtistRepository,
                mockLastfmTasteUpdatingService, mockMailer, mockEngine, mockNotifyService
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

        service.renderTemplateInstance("some@email.com");

        verify(mockTemplate, times(1))
                .data(anyString(), any());
    }

    @Test
    @DisplayName("completeRegistration() existing user")
    void completeRegistrationNew() {
        var email = "user@mail.com";
        var userStub = new User();
        userStub.setEmail(email);
        Mockito.when(mockUserRepository.findByEmailOptional(email)).thenReturn(Optional.of(userStub));

        final var user = service.completeRegistration(email);

        assertEquals(userStub.getEmail(), user.getEmail());
        verify(mockUserFactory, never()).createUser(email);
    }

    @Test
    @DisplayName("completeRegistration() new user created gets Welcome mail")
    void completeRegistrationExisting() {
        var email = "user@mail.com";
        var userStub = new User();
        userStub.setEmail(email);
        Mockito.when(mockUserRepository.findByEmailOptional(email)).thenReturn(Optional.empty());
        Mockito.when(mockUserFactory.createUser(email)).thenReturn(userStub);

        final var user = service.completeRegistration(email);

        assertEquals(userStub.getEmail(), user.getEmail());
        verify(mockUserFactory, times(1)).createUser(email);
        verify(mockMailer, times(1)).sendWelcome(eq(userStub), any(), any());
    }

    @Test
    @DisplayName("followArtist() ArtistDto `id` provided")
    void followArtist() {
        var user = new User();
        user.id = 1L;
        when(mockUserRepository.findByEmail(any())).thenReturn(user);
        var artistDto = ArtistDto.builder()
                .name("name")
                .id("3")
                .build();

        service.followArtist("some@mail.com", artistDto);

        verify(mockUserArtistRepository, times(1)).persist(any(UserArtist.class));
        verify(mockNotifyService, times(1)).notifySingleUser(any(), any());
        verify(mockArtistRepository, never()).persist(any(Artist.class));
    }

    @Test
    @DisplayName("followArtist() ArtistDto `id` not provided, should create artist")
    void followArtistNoId() {
        var user = new User();
        user.id = 1L;
        when(mockUserRepository.findByEmail(any())).thenReturn(user);
        var artistDto = ArtistDto.builder()
                .name("name")
                .id(null)
                .build();

        service.followArtist("some@mail.com", artistDto);

        verify(mockArtistRepository, times(1)).persist(any(Artist.class));
        verify(mockNotifyService, never()).notifySingleUser(any(), any());
        verify(mockUserArtistRepository, times(1)).persist(any(UserArtist.class));
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
        Optional<String> lastfmUserNameOpt = Optional.of("lastfm");
        Optional<Boolean> enableSpotifyOpt = Optional.empty();

        assertThrows(NotFoundException.class,
                () -> service.enableTasteSources(email, lastfmUserNameOpt, enableSpotifyOpt));
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

    @Test
    void enableLastfmAndSpotify() {
        var email = "user@mail.com";
        var userStub = new User();
        userStub.setEmail(email);
        Mockito.when(mockUserRepository.findByEmailOptional(email)).thenReturn(Optional.of(userStub));

        final var user = service.enableTasteSources(email, Optional.of("lastfm"), Optional.of(Boolean.TRUE));

        assertEquals(Boolean.TRUE, user.getSpotifyEnabled());
        assertEquals("lastfm", user.getLastfmUsername());
    }
}