package com.raccoon.entity.factory;

import com.raccoon.entity.Artist;
import com.raccoon.entity.RaccoonUser;
import com.raccoon.entity.UserArtist;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserArtistFactory {

    public UserArtist createUserArtist(final RaccoonUser user,
                                       final Artist artist,
                                       final float weight) {
        var userArtist = new UserArtist();
        userArtist.setArtist(artist);
        userArtist.setUser(user);
        userArtist.setWeight(weight);

        return userArtist;
    }

}
