package com.raccoon.notify;

import com.raccoon.entity.Artist;
import com.raccoon.entity.User;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.repository.ReleaseRepository;
import com.raccoon.entity.repository.UserArtistRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotifyServiceTest {

    NotifyService notifyService;

    @Mock
    ReleaseRepository mockReleaseRepository;
    @Mock
    UserArtistRepository mockUserArtistRepository;
    @Mock
    MailingService mockMailingService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should notify nobody")
    void notifyNobody() {
        notifyService = new NotifyService(mockReleaseRepository, mockUserArtistRepository, mockMailingService);

        final var usersNotified = notifyService.notifyUsers();

        assertEquals(Collections.EMPTY_LIST, usersNotified);
    }

    @Test
    @DisplayName("A sent message should result in the user getting returned")
    void notifySingleUserSuccess() {
        User user = new User();
        Artist artist = new Artist();
        UserArtist ua = new UserArtist();
        ua.setUser(user);
        ua.setArtist(artist);
        when(mockUserArtistRepository.getUserArtistsWithNewRelease()).thenReturn(List.of(ua));
        when(mockMailingService.send(any(), any(User.class), any())).thenReturn(Boolean.TRUE);

        notifyService = new NotifyService(mockReleaseRepository, mockUserArtistRepository, mockMailingService);

        final var usersNotified = notifyService.notifyUsers();

        assertEquals(1, usersNotified.size());
        verify(mockMailingService, times(1)).send(any(), any(User.class), any());
        verify(mockUserArtistRepository, times(1)).persist(any(Iterable.class));
    }

}