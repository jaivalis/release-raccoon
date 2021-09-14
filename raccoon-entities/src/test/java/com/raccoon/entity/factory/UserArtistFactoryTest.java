package com.raccoon.entity.factory;

import com.raccoon.entity.Artist;
import com.raccoon.entity.User;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.repository.ArtistRepository;
import com.raccoon.entity.repository.UserArtistRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserArtistFactoryTest {

    UserArtistFactory factory;

    @Mock
    ArtistRepository artistRepository;
    @Mock
    UserArtistRepository userArtistRepository;

    @BeforeEach
    void setUp() {
        factory = new UserArtistFactory(artistRepository, userArtistRepository);
    }

    @Test
    void getOrCreateUserArtistExists() {
        var user = new User();
        user.id = 1L;
        var artist = new Artist();
        artist.id = 1L;
        var userArtist = new UserArtist();
        userArtist.setUser(user);
        userArtist.setArtist(artist);
        when(userArtistRepository.findByUserArtistOptional(any(), any()))
                .thenReturn(Optional.of(userArtist));

        factory.getOrCreateUserArtist(user, artist);

        verify(artistRepository, times(0)).persist(any(Artist.class));
    }

    @Test
    @DisplayName("No ids provided to the entities")
    void getOrCreateUserArtistCreateNoId() {
        var artistName = "artist";
        var user = new User();
        user.id = null;
        var artist = new Artist();
        artist.setName(artistName);
        artist.id = null;

        var created = factory.getOrCreateUserArtist(user, artist);

        assertEquals(artistName, created.getArtist().getName());
        verify(artistRepository, times(1)).persist(any(Artist.class));
        verify(userArtistRepository, times(1)).persist(any(UserArtist.class));
    }

    @Test
    void getOrCreateUserArtistCreate() {
        var artistName = "artist";
        var user = new User();
        user.id = 1L;
        var artist = new Artist();
        artist.setName(artistName);
        artist.id = 1L;
        var userArtist = new UserArtist();
        when(userArtistRepository.findByUserArtistOptional(any(), any()))
                .thenReturn(Optional.empty());

        var created = factory.getOrCreateUserArtist(user, artist);

        assertEquals(artistName, created.getArtist().getName());
        verify(artistRepository, times(1)).persist(any(Artist.class));
        verify(userArtistRepository, times(1)).persist(any(UserArtist.class));
    }
}