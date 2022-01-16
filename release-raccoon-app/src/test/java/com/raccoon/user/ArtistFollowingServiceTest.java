package com.raccoon.user;

import com.raccoon.entity.Artist;
import com.raccoon.entity.User;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.repository.ArtistRepository;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.notify.NotifyService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@ExtendWith(MockitoExtension.class)
class ArtistFollowingServiceTest {

    ArtistFollowingService service;

    @Mock
    UserRepository mockUserRepository;
    @Mock
    ArtistRepository mockArtistRepository;
    @Mock
    UserArtistRepository mockUserArtistRepository;
    @Mock
    NotifyService mockNotifyService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        service = new ArtistFollowingService(
                mockUserRepository, mockArtistRepository, mockUserArtistRepository, mockNotifyService
        );

        // user is expected to be present
        var user = new User();
        user.id = 1L;
        when(mockUserRepository.findByEmail(any())).thenReturn(user);
    }

    @Test
    @DisplayName("followArtist() ArtistDto name already in db, should: notify")
    void followArtist() {
        // Artist existed in the database prior, should look for relevant releases and notify user
        var existingArtist = new Artist();
        existingArtist.setCreateDate(LocalDateTime.now().minusDays(2));

        service.followArtist("some@mail.com", existingArtist);

        verify(mockUserArtistRepository, times(1)).persist(any(UserArtist.class));
        verify(mockNotifyService, times(1)).notifySingleUser(any(), any());
    }

    @Test
    @DisplayName("followArtist() ArtistDto `id` newly created artist, should: not notify")
    void followArtistNoId() {
        var existingArtist = new Artist();
        existingArtist.setCreateDate(LocalDateTime.now());

        service.followArtist("some@mail.com", existingArtist);

        verify(mockNotifyService, never()).notifySingleUser(any(), any());
        verify(mockUserArtistRepository, times(1)).persist(any(UserArtist.class));
    }

    @Test
    void unfollowArtist() {
        service.unfollowArtist("some@mail.com", 2L);

        verify(mockUserArtistRepository).deleteAssociation(1L, 2L);
    }
}