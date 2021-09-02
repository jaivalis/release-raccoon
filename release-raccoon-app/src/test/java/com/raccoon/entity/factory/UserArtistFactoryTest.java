package com.raccoon.entity.factory;

import com.raccoon.entity.Artist;
import com.raccoon.entity.User;
import com.raccoon.entity.repository.ArtistRepository;
import com.raccoon.entity.repository.UserArtistRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserArtistFactoryTest {

    UserArtistFactory factory;

    @Mock
    ArtistRepository artistRepository;
    @Mock
    UserArtistRepository userArtistRepository;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        factory = new UserArtistFactory(artistRepository, userArtistRepository);
    }

    @Test
    @DisplayName("Non existent artist, creates new")
    void registrationConflict() {
        User userStub = new User();
        Artist artistStub = new Artist();

        factory.getOrCreateUserArtist(userStub, artistStub);

        verify(artistRepository, times(1)).persist(artistStub);
    }

}
