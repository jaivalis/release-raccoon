package com.raccoon.notify;

import com.raccoon.entity.Artist;
import com.raccoon.entity.RaccoonUser;
import com.raccoon.entity.Release;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.repository.ReleaseRepository;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.entity.repository.UserSettingsRepository;
import com.raccoon.mail.RaccoonMailer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

import io.smallrye.mutiny.Uni;

import static java.util.Collections.emptyList;
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
    UserSettingsRepository userSettingsRepository;
    @Mock
    RaccoonMailer mockMailer;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        notifyService = new NotifyService(
                mockReleaseRepository,
                mockUserArtistRepository,
                userSettingsRepository,
                mockMailer
        );
    }

    @Test
    @DisplayName("Should notify nobody")
    void notifyUsersNotifyNobody() {
        notifyService.notifyUsers();

        verify(mockUserArtistRepository, times(0)).persist(anyList());
        verify(mockUserArtistRepository, times(0)).persist(any(UserArtist.class));
    }

    @Test
    @DisplayName("Mail send success, raccoonUser gets updated")
    void notifyUsersSuccess() {
        RaccoonUser raccoonUser = new RaccoonUser();
        raccoonUser.setEmail("email");
        Artist artist = new Artist();
        UserArtist ua = new UserArtist();
        ua.setUser(raccoonUser);
        ua.setArtist(artist);
        when(mockUserArtistRepository.getUserArtistsWithNewRelease()).thenReturn(List.of(ua));

        Uni<Boolean> uni = notifyService.notifyUsers();

        var success = uni.await().atMost(Duration.ofSeconds(1));
        assertTrue(success);
    }

    @Test
    @DisplayName("Mail send failure, no users modified")
    void notifyUsersMailFailure() {
        RaccoonUser raccoonUser = new RaccoonUser();
        raccoonUser.setEmail("email");
        Artist artist = new Artist();
        UserArtist ua = new UserArtist();
        ua.setUser(raccoonUser);
        ua.setArtist(artist);
        when(mockUserArtistRepository.getUserArtistsWithNewRelease()).thenReturn(List.of(ua));
        Uni<Void> failedUni = Uni.createFrom().failure(IllegalArgumentException::new);
        when(mockMailer.sendDigest(eq(raccoonUser), anyList(), any(), any())).thenReturn(failedUni);

        Uni<Boolean> uni = notifyService.notifyUsers();

        var success = uni.await().atMost(Duration.ofSeconds(1));
        assertFalse(success);
    }

    @Test
    @DisplayName("notifySingleUser(), 1 mail to send")
    void notifySingleUserMailSent() {
        RaccoonUser raccoonUser = new RaccoonUser();
        raccoonUser.setEmail("email");
        Artist artist = new Artist();
        UserArtist ua = new UserArtist();
        ua.setUser(raccoonUser);
        ua.setArtist(artist);
        Uni<Void> failedUni = Uni.createFrom().failure(IllegalArgumentException::new);
        when(mockMailer.sendDigest(eq(raccoonUser), anyList(), any(), any())).thenReturn(failedUni);
        Release release = new Release();
        Collection<UserArtist> mightHaveNewReleases = List.of(ua);
        when(mockReleaseRepository.findByArtistsSinceDays(anySet(), anyInt())).thenReturn(List.of(release));

        notifyService.notifySingleUser(raccoonUser, mightHaveNewReleases);

        verify(mockMailer, times(1)).sendDigest(eq(raccoonUser), eq(List.of(release)), any(), any());
    }

    @Test
    @DisplayName("notifySingleUser(), no mails sent")
    void notifySingleUserNoMail() {
        RaccoonUser raccoonUser = new RaccoonUser();
        raccoonUser.setEmail("email");
        Artist artist = new Artist();
        UserArtist ua = new UserArtist();
        ua.setUser(raccoonUser);
        ua.setArtist(artist);
        Collection<UserArtist> mightHaveNewReleases = List.of(ua);
        when(mockReleaseRepository.findByArtistsSinceDays(anySet(), anyInt())).thenReturn(emptyList());

        notifyService.notifySingleUser(raccoonUser, mightHaveNewReleases);

        verify(mockMailer, never()).sendDigest(any(), anyList(), any(), any());
    }

    @Test
    @DisplayName("successCallback should not update any UserArtist")
    void successCallbackUpdatesUserArtist() {
        RaccoonUser raccoonUser = new RaccoonUser();
        raccoonUser.setEmail("email");
        Artist artist = new Artist();
        UserArtist ua = new UserArtist();
        ua.setUser(raccoonUser);
        ua.setArtist(artist);

        notifyService.mailSuccessCallback(raccoonUser, List.of(ua));

        assertFalse(ua.hasNewRelease);
        verify(mockUserArtistRepository, times(1)).persist(anyList());
    }

    @Test
    @DisplayName("failCallback should not update any UserArtist")
    void failCallbackUpdatesUserArtist() {
        RaccoonUser raccoonUser = new RaccoonUser();

        notifyService.mailFailureCallback(raccoonUser);

        verify(mockUserArtistRepository, never()).persist(anyList());
        verify(mockUserArtistRepository, never()).persist(any(UserArtist.class));
    }

}