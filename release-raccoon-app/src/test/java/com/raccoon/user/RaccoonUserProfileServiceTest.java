package com.raccoon.user;

import com.raccoon.dto.ArtistDto;
import com.raccoon.dto.mapping.ArtistMapper;
import com.raccoon.entity.Artist;
import com.raccoon.entity.RaccoonUser;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.factory.UserFactory;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.mail.RaccoonMailer;
import com.raccoon.search.dto.SearchResultArtistDto;
import com.raccoon.taste.lastfm.LastfmTasteUpdatingService;
import com.raccoon.user.settings.UserSettingsService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.NotFoundException;

import static com.raccoon.templatedata.QuteTemplateLoader.PROFILE_TEMPLATE_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RaccoonUserProfileServiceTest {

    UserProfileService service;

    @Mock
    UserRepository mockUserRepository;
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
    ArtistFollowingService mockArtistFollowingService;
    @Mock
    ArtistMapper mockArtistMapper;
    @Mock
    UserSettingsService mockUserSettingsService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(mockEngine.getTemplate(PROFILE_TEMPLATE_ID)).thenReturn(mockTemplate);

        service = new UserProfileService(
                mockUserRepository, mockUserFactory, mockUserArtistRepository, mockLastfmTasteUpdatingService,
                mockMailer, mockEngine, mockArtistFollowingService, mockArtistMapper, mockUserSettingsService
        );
    }

    @Test
    void getUserArtists() {
        var user = new RaccoonUser();
        user.id = 1L;

        service.getUserArtists(user);

        verify(mockUserArtistRepository, times(1)).findByUserIdSortedByWeight(1L);
    }

    @Test
    void getTemplateInstance() {
        var user = new RaccoonUser();
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
    @DisplayName("completeRegistration() existing raccoonUser")
    void completeRegistrationNew() {
        var email = "raccoonUser@mail.com";
        var userStub = new RaccoonUser();
        userStub.setEmail(email);
        when(mockUserRepository.findByEmailOptional(email)).thenReturn(Optional.of(userStub));

        final var user = service.completeRegistration(email);

        assertEquals(userStub.getEmail(), user.getEmail());
        verify(mockUserFactory, never()).createUser(email);
    }

    @Test
    @DisplayName("completeRegistration() new raccoonUser created gets Welcome mail")
    void completeRegistrationExisting() {
        var email = "raccoonUser@mail.com";
        var userStub = new RaccoonUser();
        userStub.setEmail(email);
        when(mockUserRepository.findByEmailOptional(email)).thenReturn(Optional.empty());
        when(mockUserFactory.createUser(email)).thenReturn(userStub);

        final var user = service.completeRegistration(email);

        assertEquals(userStub.getEmail(), user.getEmail());
        verify(mockUserFactory, times(1)).createUser(email);
        verify(mockMailer, times(1)).sendWelcome(eq(userStub), any(), any());
    }

    @Test
    @DisplayName("followArtist()")
    void followArtist() {
        var mail = "some@mail.com";
        var artistDto = SearchResultArtistDto.builder().build();
        var artist = new Artist();
        when(mockArtistMapper.fromDto(artistDto)).thenReturn(artist);

        service.followArtist("some@mail.com", artistDto);

        verify(mockArtistFollowingService, times(1)).followArtist(mail, artist);
    }

    @Test
    void unfollowArtist() {
        var mail = "some@mail.com";
        var artistId = 2L;

        service.unfollowArtist("some@mail.com", artistId);

        verify(mockArtistFollowingService, times(1)).unfollowArtist(mail, artistId);
    }

    @Test
    @DisplayName("raccoonUser not found, returns 404")
    void enableSourceNotExistent() {
        var email = "raccoonUser@mail.com";
        when(mockUserRepository.findByEmailOptional(email)).thenReturn(Optional.empty());
        Optional<String> lastfmUserNameOpt = Optional.of("lastfm");
        Optional<Boolean> enableSpotifyOpt = Optional.empty();

        assertThrows(NotFoundException.class,
                () -> service.enableTasteSources(email, lastfmUserNameOpt, enableSpotifyOpt));
    }

    @Test
    void enableLastfm() {
        var email = "raccoonUser@mail.com";
        var userStub = new RaccoonUser();
        userStub.setEmail(email);
        when(mockUserRepository.findByEmailOptional(email)).thenReturn(Optional.of(userStub));

        final var user = service.enableTasteSources(email, Optional.of("lastfm"), Optional.empty());

        assertEquals("lastfm", user.getLastfmUsername());
        assertFalse(user.getSpotifyEnabled());
    }

    @Test
    void enableSpotify() {
        var email = "raccoonUser@mail.com";
        var userStub = new RaccoonUser();
        userStub.setEmail(email);
        when(mockUserRepository.findByEmailOptional(email)).thenReturn(Optional.of(userStub));

        final var user = service.enableTasteSources(email, Optional.empty(), Optional.of(Boolean.TRUE));

        assertNull(user.getLastfmUsername());
        assertEquals(Boolean.TRUE, user.getSpotifyEnabled());
    }

    @Test
    void enableLastfmAndSpotify() {
        var email = "raccoonUser@mail.com";
        var userStub = new RaccoonUser();
        userStub.setEmail(email);
        when(mockUserRepository.findByEmailOptional(email)).thenReturn(Optional.of(userStub));

        final var user = service.enableTasteSources(email, Optional.of("lastfm"), Optional.of(Boolean.TRUE));

        assertEquals(Boolean.TRUE, user.getSpotifyEnabled());
        assertEquals("lastfm", user.getLastfmUsername());
    }

    @Test
    @DisplayName("getFollowedArtists(): returns list")
    void getFollowedArtists() {
        var email = "raccoonUser@mail.com";
        var stubUser = new RaccoonUser();
        stubUser.setEmail(email);
        stubUser.id = 9L;

        Artist stubArtist = new Artist();
        stubArtist.setId(9L);
        stubArtist.setName("name");
        stubArtist.setSpotifyUri("spotify");
        stubArtist.setLastfmUri("lastfm");
        stubArtist.setCreateDate(LocalDateTime.now());
        UserArtist userArtist = new UserArtist();
        userArtist.setArtist(stubArtist);
        userArtist.setUser(stubUser);
        when(mockUserArtistRepository.findByUserIdSortedByWeight(stubUser.id)).thenReturn(List.of(userArtist));

        when(mockUserRepository.findByEmail(email)).thenReturn(stubUser);
        var dto = mock(ArtistDto.class);
        when(mockArtistMapper.toArtistDto(stubArtist)).thenReturn(dto);

        final var response = service.getFollowedArtists(email);

        assertEquals(1, response.getTotal());
        assertNotNull(response.getRows());
        assertNotNull(response.getRows().getFirst());
        assertEquals(dto, response.getRows().getFirst());
    }

    @Test
    @DisplayName("getFollowedArtists(): returns empty list")
    void getFollowedArtistsEmptyList() {
        var email = "raccoonUser@mail.com";
        var stubUser = new RaccoonUser();
        stubUser.setEmail(email);
        stubUser.id = 9L;
        when(mockUserRepository.findByEmail(email)).thenReturn(stubUser);
        when(mockUserArtistRepository.findByUserIdSortedByWeight(stubUser.id)).thenReturn(Collections.emptyList());

        final var response = service.getFollowedArtists(email);

        assertNotNull(response);
        assertEquals(0, response.getTotal());
        assertTrue(response.getRows().isEmpty());
    }

    @Test
    @DisplayName("getFollowedArtists(): returns paginated results when page and size provided")
    void getFollowedArtists_should_returnPaginatedResults_when_pageAndSizeProvided() {
        var email = "raccoonUser@mail.com";
        var stubUser = new RaccoonUser();
        stubUser.setEmail(email);
        stubUser.id = 9L;

        Artist stubArtist1 = new Artist();
        stubArtist1.setId(1L);
        stubArtist1.setName("Artist 1");
        UserArtist userArtist1 = new UserArtist();
        userArtist1.setArtist(stubArtist1);
        userArtist1.setUser(stubUser);

        Artist stubArtist2 = new Artist();
        stubArtist2.setId(2L);
        stubArtist2.setName("Artist 2");
        UserArtist userArtist2 = new UserArtist();
        userArtist2.setArtist(stubArtist2);
        userArtist2.setUser(stubUser);

        when(mockUserRepository.findByEmail(email)).thenReturn(stubUser);
        when(mockUserArtistRepository.findByUserIdSortedByWeight(eq(stubUser.id), any())).thenReturn(List.of(userArtist1));
        when(mockUserArtistRepository.countByUserId(stubUser.id)).thenReturn(2L);

        var dto1 = mock(ArtistDto.class);
        when(mockArtistMapper.toArtistDto(stubArtist1)).thenReturn(dto1);

        final var response = service.getFollowedArtists(email, Optional.of(0), Optional.of(1));

        assertEquals(2, response.getTotal()); // Total count
        assertEquals(1, response.getRows().size()); // Page size
        assertEquals(dto1, response.getRows().getFirst());
        verify(mockUserArtistRepository).findByUserIdSortedByWeight(eq(stubUser.id), any());
        verify(mockUserArtistRepository).countByUserId(stubUser.id);
    }

    @Test
    @DisplayName("getFollowedArtists(): backwards compatibility - no pagination params returns all results")
    void getFollowedArtists_should_returnAllResults_when_noPaginationParams() {
        var email = "raccoonUser@mail.com";
        var stubUser = new RaccoonUser();
        stubUser.setEmail(email);
        stubUser.id = 9L;

        Artist stubArtist = new Artist();
        stubArtist.setId(9L);
        UserArtist userArtist = new UserArtist();
        userArtist.setArtist(stubArtist);
        userArtist.setUser(stubUser);

        when(mockUserRepository.findByEmail(email)).thenReturn(stubUser);
        when(mockUserArtistRepository.findByUserIdSortedByWeight(stubUser.id)).thenReturn(List.of(userArtist));

        var dto = mock(ArtistDto.class);
        when(mockArtistMapper.toArtistDto(stubArtist)).thenReturn(dto);

        final var response = service.getFollowedArtists(email, Optional.empty(), Optional.empty());

        assertEquals(1, response.getTotal());
        assertEquals(1, response.getRows().size());
        assertEquals(dto, response.getRows().getFirst());
        verify(mockUserArtistRepository).findByUserIdSortedByWeight(stubUser.id);
        verify(mockUserArtistRepository, never()).countByUserId(anyLong());
    }

}
