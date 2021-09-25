package com.raccoon.user;

import com.raccoon.entity.User;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.entity.repository.UserRepository;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.quarkus.qute.Template;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class UserProfileService {

    UserRepository userRepository;
    UserArtistRepository userArtistRepository;

    @Inject
    Template profile;

    @Inject
    public UserProfileService(final UserRepository userRepository,
                              final UserArtistRepository userArtistRepository,
                              final Template profile) {
        this.userRepository = userRepository;
        this.userArtistRepository = userArtistRepository;
        this.profile = profile;
    }


    public List<UserArtist> getUserArtists(final User user) {
        return userArtistRepository.findByUserIdByWeight(user.id);
    }

    public String getTemplateInstance(final String userEmail) {
        var user = userRepository.findByEmail(userEmail);
        boolean isSpotifyEnabled = user.getSpotifyEnabled();
        var lastFmUsername = user.getLastfmUsername();
        return profile.data(
                "isSpotifyEnabled", isSpotifyEnabled,
                "isLastfmEnabled", lastFmUsername != null,
                "lastfmUsername", lastFmUsername,
                "artistsFollowed", getUserArtists(user)
        ).render();
    }
    
    public void unfollowArtist(final String userEmail, final Long artistId) {
        var user = userRepository.findByEmail(userEmail);
        userArtistRepository.deleteAssociation(user.id, artistId);
    }

//    public void followArtist(final String userEmail, final Long artistId) {
//        var user = userRepository.findByEmail(userEmail);
//        userArtistRepository.followArtist(user.id, artistName);
//    }
}
