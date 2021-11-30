package com.raccoon.notify;

import com.raccoon.entity.Artist;
import com.raccoon.entity.User;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.repository.ReleaseRepository;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.mail.RaccoonMailer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;

import io.smallrye.mutiny.Uni;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotifyServiceTest {

    NotifyService notifyService;

    @Mock
    ReleaseRepository mockReleaseRepository;
    @Mock
    UserArtistRepository mockUserArtistRepository;
    @Mock
    RaccoonMailer mockMailer;

    @Captor
    ArgumentCaptor<List<UserArtist>> userArtistListCaptor;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        notifyService = new NotifyService(
                mockReleaseRepository,
                mockUserArtistRepository,
                mockMailer
        );
    }

    @Test
    @DisplayName("notifyCronJob() calls notifyUsers()")
    void notifyCronJob() {
        NotifyService notifyService = mock(NotifyService.class);
        doCallRealMethod().when(notifyService).notifyCronJob();

        notifyService.notifyCronJob();

        verify(notifyService, times(1)).notifyUsers();
    }

    @Test
    @DisplayName("Should notify nobody")
    void notifyNobody() {
        notifyService.notifyUsers();

        verify(mockUserArtistRepository, times(0)).persist(anyList());
        verify(mockUserArtistRepository, times(0)).persist(any(UserArtist.class));
    }

    @Test
    @DisplayName("Mail send success, user gets updated")
    void notifySingleUserSuccess() {
        User user = new User();
        user.setEmail("email");
        Artist artist = new Artist();
        UserArtist ua = new UserArtist();
        ua.setUser(user);
        ua.setArtist(artist);
        when(mockUserArtistRepository.getUserArtistsWithNewRelease()).thenReturn(List.of(ua));
        doNothing().when(mockUserArtistRepository).persist(anyList());
        Uni<Void> completeUni = Uni.createFrom().voidItem();
        when(mockMailer.sendDigest(eq(user), anyList())).thenReturn(completeUni);

        Uni<Boolean> uni = notifyService.notifyUsers();

        var success = uni.await().atMost(Duration.ofSeconds(1));
        assertTrue(success);
        verify(mockUserArtistRepository, times(1)).persist(userArtistListCaptor.capture());
        assertEquals(1, userArtistListCaptor.getValue().size());
        assertEquals(user, userArtistListCaptor.getValue().get(0).getUser());
        assertEquals(artist, userArtistListCaptor.getValue().get(0).getArtist());
    }

    @Test
    @DisplayName("Mail send failure, no users modified")
    void notifyUserMailFailure() {
        User user = new User();
        user.setEmail("email");
        Artist artist = new Artist();
        UserArtist ua = new UserArtist();
        ua.setUser(user);
        ua.setArtist(artist);
        when(mockUserArtistRepository.getUserArtistsWithNewRelease()).thenReturn(List.of(ua));
        Uni<Void> failedUni = Uni.createFrom().failure(IllegalArgumentException::new);
        when(mockMailer.sendDigest(eq(user), anyList())).thenReturn(failedUni);

        Uni<Boolean> uni = notifyService.notifyUsers();

        var success = uni.await().atMost(Duration.ofSeconds(1));
        assertFalse(success);
        verify(mockUserArtistRepository, never()).persist(anyList());
        verify(mockUserArtistRepository, never()).persist(any(UserArtist.class));
    }

}