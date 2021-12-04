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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;

import io.smallrye.mutiny.Uni;

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

        Uni<Boolean> uni = notifyService.notifyUsers();

        var success = uni.await().atMost(Duration.ofSeconds(1));
        assertTrue(success);
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
        when(mockMailer.sendDigest(eq(user), anyList(), any(), any())).thenReturn(failedUni);

        Uni<Boolean> uni = notifyService.notifyUsers();

        var success = uni.await().atMost(Duration.ofSeconds(1));
        assertFalse(success);
    }

    @Test
    @DisplayName("successCallback should not update any UserArtist")
    void successCallbackUpdatesUserArtist() {
        User user = new User();
        user.setEmail("email");
        Artist artist = new Artist();
        UserArtist ua = new UserArtist();
        ua.setUser(user);
        ua.setArtist(artist);

        notifyService.mailSuccessCallback(user, List.of(ua));

        assertFalse(ua.getHasNewRelease());
        verify(mockUserArtistRepository, times(1)).persist(anyList());
    }

    @Test
    @DisplayName("failCallback should not update any UserArtist")
    void failCallbackUpdatesUserArtist() {
        User user = new User();

        notifyService.mailFailureCallback(user);

        verify(mockUserArtistRepository, never()).persist(anyList());
        verify(mockUserArtistRepository, never()).persist(any(UserArtist.class));
    }

}