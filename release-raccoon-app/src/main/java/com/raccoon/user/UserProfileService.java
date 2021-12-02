package com.raccoon.user;

import com.raccoon.entity.User;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.taste.lastfm.LastfmTasteUpdatingService;
import com.raccoon.templatedata.ProfileContents;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;

import io.netty.util.internal.StringUtil;
import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;
import lombok.extern.slf4j.Slf4j;

import static com.raccoon.templatedata.QuteTemplateLoader.PROFILE_TEMPLATE_ID;

@Slf4j
@ApplicationScoped
public class UserProfileService {

    UserRepository userRepository;
    UserArtistRepository userArtistRepository;
    LastfmTasteUpdatingService lastfmTasteUpdatingService;
    Template profile;

    @Inject
    public UserProfileService(final UserRepository userRepository,
                              final UserArtistRepository userArtistRepository,
                              final LastfmTasteUpdatingService lastfmTasteUpdatingService,
                              final Engine engine) {
        this.userRepository = userRepository;
        this.userArtistRepository = userArtistRepository;
        this.lastfmTasteUpdatingService = lastfmTasteUpdatingService;
        this.profile = engine.getTemplate(PROFILE_TEMPLATE_ID);
    }


    public List<UserArtist> getUserArtists(final User user) {
        return userArtistRepository.findByUserIdByWeight(user.id);
    }

    public String renderTemplateInstance(final String userEmail) {
        var user = userRepository.findByEmail(userEmail);
        boolean isSpotifyEnabled = user.getSpotifyEnabled();
        var lastFmUsername = user.getLastfmUsername();
        var canScrapeSpotify = isSpotifyEnabled && user.isSpotifyScrapeRequired(7);
        var canScrapeLastFm = !StringUtil.isNullOrEmpty(lastFmUsername) && user.isLastfmScrapeRequired(7);
        log.info("lastFmUsername {}, isSpotifyEnabled {}, showScrapeSpotifyButton {}, showScrapeLastfmButton {}",
                lastFmUsername, isSpotifyEnabled, canScrapeSpotify, canScrapeLastFm);
        ProfileContents contents = ProfileContents.builder()
                .spotifyEnabled(isSpotifyEnabled)
                .canScrapeSpotify(canScrapeSpotify)
                .lastfmEnabled(lastFmUsername != null)
                .canScrapeLastfm(canScrapeLastFm)
                .artistsFollowed(getUserArtists(user))
                .build();
        return profile.data(
                "contents", contents
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
