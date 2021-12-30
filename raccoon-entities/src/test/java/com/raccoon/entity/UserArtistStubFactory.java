package com.raccoon.entity;

import com.raccoon.entity.factory.ArtistFactory;
import com.raccoon.entity.factory.UserFactory;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.entity.repository.UserRepository;

public class UserArtistStubFactory {

    UserArtistRepository userArtistRepository;
    UserRepository userRepository;
    UserFactory userFactory;
    ArtistFactory artistFactory;

    public UserArtistStubFactory(UserArtistRepository userArtistRepository,
                                 UserFactory userFactory,
                                 UserRepository userRepository,
                                 ArtistFactory artistFactory) {
        this.userArtistRepository = userArtistRepository;
        this.userFactory = userFactory;
        this.userRepository = userRepository;
        this.artistFactory = artistFactory;
    }

    public UserArtist stubUserArtist(String username, String artistName) {
        var user = stubUser(username);
        var artist = stubArtist(artistName);

        var userArtist = new UserArtist();
        userArtist.setUser(user);
        userArtist.setArtist(artist);
        userArtistRepository.persist(userArtist);
        return userArtist;
    }

    User stubUser(String email) {
        return userRepository.findByEmailOptional(email)
                .orElseGet(() -> userFactory.createUser(email));
    }


    Artist stubArtist(String name) {
        return artistFactory.getOrCreateArtist(name);
    }

}
