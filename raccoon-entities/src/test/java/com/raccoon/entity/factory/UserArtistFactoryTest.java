package com.raccoon.entity.factory;

import com.raccoon.entity.Artist;
import com.raccoon.entity.User;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class UserArtistFactoryTest {

    UserArtistFactory factory = new UserArtistFactory();

    @Test
    void getOrCreateUserArtistExists() {
        var user = new User();
        user.id = 1L;
        var artist = new Artist();
        artist.id = 1L;
        var weight = 0.99f;

        var userArtist = factory.createUserArtist(user, artist, weight);

        assertEquals(user, userArtist.getUser());
        assertEquals(artist, userArtist.getArtist());
        assertEquals(weight, userArtist.getWeight());
    }

}