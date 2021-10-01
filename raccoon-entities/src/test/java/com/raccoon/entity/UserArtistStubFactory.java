package com.raccoon.entity;

import com.raccoon.entity.factory.ArtistFactory;
import com.raccoon.entity.factory.UserFactory;
import com.raccoon.entity.repository.UserArtistRepository;

public class UserArtistStubFactory {

    UserArtistRepository repository;
    UserFactory userFactory;
    ArtistFactory artistFactory;

    public UserArtistStubFactory(UserArtistRepository repository,
                                 UserFactory userFactory,
                                 ArtistFactory artistFactory) {
        this.repository = repository;
        this.userFactory = userFactory;
        this.artistFactory = artistFactory;
    }

    public UserArtist stubUserArtist(String username, String artistName) {
        var user = stubUser(username);
        var artist = stubArtist(artistName);

        var userArtist = new UserArtist();
        userArtist.setUser(user);
        userArtist.setArtist(artist);
        repository.persist(userArtist);
        return userArtist;
    }

    User stubUser(String email) {
        return userFactory.getOrCreateUser(email);
    }


    Artist stubArtist(String name) {
        return artistFactory.getOrCreateArtist(name);
    }

}
