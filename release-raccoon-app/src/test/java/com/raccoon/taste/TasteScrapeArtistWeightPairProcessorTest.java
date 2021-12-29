package com.raccoon.taste;

import com.raccoon.entity.Artist;
import com.raccoon.entity.User;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.factory.UserArtistFactory;
import com.raccoon.entity.repository.ArtistRepository;
import com.raccoon.entity.repository.UserArtistRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@ExtendWith(MockitoExtension.class)
class TasteScrapeArtistWeightPairProcessorTest {

    TasteScrapeArtistWeightPairProcessor processor;

    @Mock
    ArtistRepository mockArtistRepository;
    @Mock
    UserArtistFactory mockUserArtistFactory;
    @Mock
    UserArtistRepository mockUserArtistRepository;

    @BeforeEach
    void setUp() {
        openMocks(this);
        processor = new TasteScrapeArtistWeightPairProcessor(mockArtistRepository, mockUserArtistFactory, mockUserArtistRepository);
    }

    @Test
    @DisplayName("Artist present in db, should be added to the userArtistsSet")
    void testArtistExisted() {
        UserArtist stubUserArtist = stubUserArtist();
        User user = stubUserArtist.getUser();
        Artist artist = stubUserArtist.getArtist();
        artist.setCreateDate(LocalDateTime.now().minusDays(7));
        Set<UserArtist> userArtists = new HashSet<>();
        when(mockArtistRepository.findByNameOptional(artist.getName())).thenReturn(Optional.of(artist));
        when(mockUserArtistRepository.findByUserIdArtistIdOptional(anyLong(), anyLong())).thenReturn(Optional.of(stubUserArtist));

        processor.delegateProcessArtistWeightPair(user, artist, 0.9F, userArtists);

        assertEquals(1, userArtists.size());
        verify(mockArtistRepository, never()).persist(artist);
    }

    @Test
    @DisplayName("Artist not present in db, should not be added to the userArtistsSet")
    void testArtistNotExisted() {
        UserArtist stubUserArtist = stubUserArtist();
        User user = stubUserArtist.getUser();
        Artist artist = stubUserArtist.getArtist();
        Set<UserArtist> userArtists = new HashSet<>();
        when(mockArtistRepository.findByNameOptional(artist.getName())).thenReturn(Optional.empty());
        when(mockUserArtistRepository.findByUserIdArtistIdOptional(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(mockUserArtistFactory.createUserArtist(any(), any(), anyFloat())).thenReturn(stubUserArtist);

        processor.delegateProcessArtistWeightPair(user, artist, 0.9F, userArtists);

        assertEquals(0, userArtists.size());
        verify(mockArtistRepository, times(1)).persist(artist);
    }

    private UserArtist stubUserArtist() {
        User user = new User();
        user.id = 6L;
        Artist artist = new Artist();
        artist.setId(9L);
        artist.setName("name");
        artist.setCreateDate(LocalDateTime.now());

        UserArtist userArtist = new UserArtist();
        userArtist.setArtist(artist);
        userArtist.setUser(user);

        return userArtist;
    }
}