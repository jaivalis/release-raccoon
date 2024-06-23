package com.raccoon.entity;

import com.raccoon.entity.factory.ArtistFactory;
import com.raccoon.entity.factory.UserFactory;
import com.raccoon.entity.repository.ArtistRepository;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.entity.repository.UserRepository;

public class UserArtistStubFactory {

    final UserArtistRepository userArtistRepository;
    final UserRepository userRepository;
    final UserFactory userFactory;
    final ArtistFactory artistFactory;
    final ArtistRepository artistRepository;

    public UserArtistStubFactory(UserArtistRepository userArtistRepository,
                                 UserFactory userFactory,
                                 UserRepository userRepository,
                                 ArtistFactory artistFactory,
                                 ArtistRepository artistRepository) {
        this.userArtistRepository = userArtistRepository;
        this.userFactory = userFactory;
        this.userRepository = userRepository;
        this.artistFactory = artistFactory;
        this.artistRepository = artistRepository;
    }

    public UserArtist stubUserArtist(String email, String artistName) {
        var user = stubUser(email);
        var artist = stubArtist(artistName);

        var userArtist = new UserArtist();
        userArtist.setUser(user);
        userArtist.setArtist(artist);
        userArtistRepository.persist(userArtist);
        return userArtist;
    }

    RaccoonUser stubUser(String email) {
        return userRepository.findByEmailOptional(email)
                .orElseGet(() -> {
                    RaccoonUser created = userFactory.createUser(email);
                    userRepository.persist(created);
                    return created;
                });
    }


    Artist stubArtist(String name) {
        Artist created = artistFactory.getOrCreateArtist(name);
        artistRepository.persist(created);
        return created;
    }

}
