package com.raccoon.user;

import com.raccoon.entity.User;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.entity.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

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
    Template templateMock;
    @Mock
    TemplateInstance templateInstanceMock;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        service = new UserProfileService(mockUserRepository, mockUserArtistRepository, templateMock);
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
        when(templateMock
                .data(
                        anyString(), any(),
                        anyString(), any(),
                        anyString(), any(),
                        anyString(), any())
        ).thenReturn(templateInstanceMock);

        service.getTemplateInstance("some@email.com");

        verify(templateMock, times(1))
                .data(anyString(), any(),
                        anyString(), any(),
                        anyString(), any(),
                        anyString(), any());
    }

//    @Test
//    void unfollowArtist() {
//    }
}