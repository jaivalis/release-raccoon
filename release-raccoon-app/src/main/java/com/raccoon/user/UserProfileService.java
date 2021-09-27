package com.raccoon.user;

import com.raccoon.entity.User;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.taste.lastfm.LastfmTasteUpdatingService;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;

import io.netty.util.internal.StringUtil;
import io.quarkus.qute.Template;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class UserProfileService {

    UserRepository userRepository;
    UserArtistRepository userArtistRepository;
    LastfmTasteUpdatingService lastfmTasteUpdatingService;

    @Inject
    Template profile;

    @Inject
    public UserProfileService(final UserRepository userRepository,
                              final UserArtistRepository userArtistRepository,
                              final LastfmTasteUpdatingService lastfmTasteUpdatingService,
                              final Template profile) {
        this.userRepository = userRepository;
        this.userArtistRepository = userArtistRepository;
        this.lastfmTasteUpdatingService = lastfmTasteUpdatingService;
        this.profile = profile;
    }


    public List<UserArtist> getUserArtists(final User user) {
        return userArtistRepository.findByUserIdByWeight(user.id);
    }

    public String getTemplateInstance(final String userEmail) {
        var user = userRepository.findByEmail(userEmail);
        boolean isSpotifyEnabled = user.getSpotifyEnabled();
        var lastFmUsername = user.getLastfmUsername();
        var canScrapeSpotify = isSpotifyEnabled && user.isSpotifyScrapeRequired(7);
        var canScrapeLastFm = !StringUtil.isNullOrEmpty(lastFmUsername) && user.isLastfmScrapeRequired(7);
        return profile.data(
                "isSpotifyEnabled", isSpotifyEnabled,
//                "scrapeSpotifyButton", canScrapeSpotify,
                "isLastfmEnabled", lastFmUsername != null,
//                "scrapeLastfmButton", canScrapeLastFm,
                "lastfmUsername", lastFmUsername,
                "artistsFollowed", getUserArtists(user)
        ).render();
    }
    
    public void unfollowArtist(final String userEmail, final Long artistId) {
        var user = userRepository.findByEmail(userEmail);
        userArtistRepository.deleteAssociation(user.id, artistId);
    }

    public User enableTasteSources(final String email,
                                   final Optional<String> lastfmUsernameOpt,
                                   final Optional<Boolean> enableSpotifyOpt) {
        Optional<User> existing = userRepository.findByEmailOptional(email);
        if (existing.isEmpty()) {
            log.info("User does not exist.");
            throw new NotFoundException("User not found");
        }
        var user = existing.get();
        lastfmUsernameOpt.ifPresent(user::setLastfmUsername);
        enableSpotifyOpt.ifPresent(user::setSpotifyEnabled);

        lastfmTasteUpdatingService.updateTaste(user);

        userRepository.persist(user);
        return user;
    }

}
