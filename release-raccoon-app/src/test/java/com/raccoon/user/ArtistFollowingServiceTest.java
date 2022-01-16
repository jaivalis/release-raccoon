package com.raccoon.user;

import com.raccoon.entity.Artist;
import com.raccoon.entity.User;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.repository.ArtistRepository;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.notify.NotifyService;
import com.raccoon.search.dto.ArtistDto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

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
    @DisplayName("followArtist() ArtistDto `id` provided, should: notify")
    void followArtist() {
        var artistDto = ArtistDto.builder()
                .name("name")
                .id("3")
                .build();
        // Artist existed in the database prior, should look for relevant releases and notify user
        var existingArtist = new Artist();
        existingArtist.setCreateDate(LocalDateTime.now().minusDays(2));
        when(mockArtistRepository.findByIdAndNameOptional(3L, "name")).thenReturn(Optional.of(existingArtist));

        service.followArtist("some@mail.com", artistDto);

        verify(mockUserArtistRepository, times(1)).persist(any(UserArtist.class));
        verify(mockNotifyService, times(1)).notifySingleUser(any(), any());
        verify(mockArtistRepository, never()).persist(any(Artist.class));
    }

    @Test
    @DisplayName("followArtist() ArtistDto `id` provided but incorrect, should: create new artist, not notify")
    void followArtistIncorrectIdProvided() {
        var artistDto = ArtistDto.builder()
                .name("name")
                .id("3")
                .build();
        // Artist with name and id provided is not in the database
        var existingArtist = new Artist();
        existingArtist.setCreateDate(LocalDateTime.now().minusDays(2));
        when(mockArtistRepository.findByIdAndNameOptional(3L, "name")).thenReturn(Optional.empty());

        service.followArtist("some@mail.com", artistDto);

        verify(mockUserArtistRepository, times(1)).persist(any(UserArtist.class));
        verify(mockNotifyService, never()).notifySingleUser(any(), any());
        verify(mockArtistRepository, times(1)).persist(any(Artist.class));
    }

    @Test
    @DisplayName("followArtist() ArtistDto `id` not provided, should: create artist, not notify")
    void followArtistNoId() {
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
        service.unfollowArtist("some@mail.com", 2L);

        verify(mockUserArtistRepository).deleteAssociation(1L, 2L);
    }
}