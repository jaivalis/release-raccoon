package com.raccoon.user;

import com.raccoon.dto.ProfileDto;
import com.raccoon.entity.Artist;
import com.raccoon.entity.User;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.factory.UserFactory;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.mail.RaccoonMailer;
import com.raccoon.search.dto.ArtistDto;
import com.raccoon.search.dto.mapping.ArtistMapper;
import com.raccoon.taste.lastfm.LastfmTasteUpdatingService;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.NotFoundException;

import io.netty.util.internal.StringUtil;
import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;
import lombok.extern.slf4j.Slf4j;

import static com.raccoon.templatedata.QuteTemplateLoader.PROFILE_TEMPLATE_ID;

@Slf4j
@ApplicationScoped
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserFactory userFactory;
    private final UserArtistRepository userArtistRepository;
    private final LastfmTasteUpdatingService lastfmTasteUpdatingService;
    private final RaccoonMailer mailer;
    private final Template profile;
    private final ArtistFollowingService artistFollowingService;
    private final ArtistMapper artistMapper;

    @Inject
    public UserProfileService(final UserRepository userRepository,
                              final UserFactory userFactory,
                              final UserArtistRepository userArtistRepository,
                              final LastfmTasteUpdatingService lastfmTasteUpdatingService,
                              final RaccoonMailer mailer,
                              final Engine engine,
                              final ArtistFollowingService artistFollowingService,
                              final ArtistMapper artistMapper) {
        this.userRepository = userRepository;
        this.userFactory = userFactory;
        this.userArtistRepository = userArtistRepository;
        this.lastfmTasteUpdatingService = lastfmTasteUpdatingService;
        this.mailer = mailer;
        this.profile = engine.getTemplate(PROFILE_TEMPLATE_ID);
        this.artistFollowingService = artistFollowingService;
        this.artistMapper = artistMapper;
    }


    public List<Artist> getUserArtists(final User user) {
        return userArtistRepository.findByUserIdSortedByWeight(user.id)
                .stream()
                .map(UserArtist::getArtist)
                .toList();
    }

    /**
     * @param userEmail the user of requesting followed Artists
     * @return
     */
    @NotNull
    public FollowedArtistsResponse getFollowedArtists(final String userEmail) {
        var user = userRepository.findByEmail(userEmail);

        List<ArtistDto> rows = userArtistRepository.findByUserIdSortedByWeight(user.id)
                .stream()
                .map(UserArtist::getArtist)
                .map(artistMapper::toDto)
                .toList();
        return FollowedArtistsResponse.builder()
                .rows(rows)
                .total(rows.size())
                .build();
    }

    public String renderTemplateInstance(final String userEmail) {
        var user = userRepository.findByEmail(userEmail);
        boolean isSpotifyEnabled = user.getSpotifyEnabled();
        var lastFmUsername = user.getLastfmUsername();
        var canScrapeSpotify = isSpotifyEnabled && user.isSpotifyScrapeRequired(7);
        var canScrapeLastFm = !StringUtil.isNullOrEmpty(lastFmUsername) && user.isLastfmScrapeRequired(7);
        log.info("lastFmUsername {}, isSpotifyEnabled {}, showScrapeSpotifyButton {}, showScrapeLastfmButton {}",
                lastFmUsername, isSpotifyEnabled, canScrapeSpotify, canScrapeLastFm);
        ProfileDto contents = ProfileDto.builder()
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

    /**
     * Fetches the user from the database. Sends welcome email blocking in case the user was just created.
     * @param userEmail unique user identifier
     * @return user from the database.
     */
    public User completeRegistration(final String userEmail) {
        Optional<User> optionalUser = userRepository.findByEmailOptional(userEmail);

        return optionalUser.orElseGet(() -> {
            var user = userFactory.createUser(userEmail);
            mailer.sendWelcome(
                    user,
                    () -> {
                        log.info("Welcome sent to user {}", user.id);
                        userRepository.persist(user);
                    },
                    () -> log.error("Something went wrong while sending welcome to {}", user.id)
            ).await().indefinitely();
            return user;
        });
    }

    /**
     * Create a new UserArtist association
     * @param userEmail user requesting the follow
     * @param artistDto artistDto as it originates from an Artist search.
     */
    public void followArtist(final String userEmail, final ArtistDto artistDto) {
        artistFollowingService.followArtist(userEmail, artistMapper.fromDto(artistDto));
    }

    public void unfollowArtist(final String userEmail, final Long artistId) {
        artistFollowingService.unfollowArtist(userEmail, artistId);
    }

    public User enableTasteSources(final String userEmail,
                                   final Optional<String> lastfmUsernameOpt,
                                   final Optional<Boolean> enableSpotifyOpt) {
        Optional<User> existing = userRepository.findByEmailOptional(userEmail);
        if (existing.isEmpty()) {
            log.info("User does not exist.");
            throw new NotFoundException("User not found");
        }
        var user = existing.get();
        lastfmUsernameOpt.ifPresent(user::setLastfmUsername);
        enableSpotifyOpt.ifPresent(user::setSpotifyEnabled);

        lastfmTasteUpdatingService.updateTaste(user.id);

        userRepository.persist(user);
        return user;
    }

}
