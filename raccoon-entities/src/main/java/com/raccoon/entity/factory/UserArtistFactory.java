package com.raccoon.entity.factory;

import com.raccoon.entity.Artist;
import com.raccoon.entity.User;
import com.raccoon.entity.UserArtist;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserArtistFactory {

    public UserArtist createUserArtist(final User user,
                                       final Artist artist,
                                       final float weight) {
        var userArtist = new UserArtist();
        userArtist.setArtist(artist);
        userArtist.setUser(user);
        userArtist.setWeight(weight);

        return userArtist;
    }

}
