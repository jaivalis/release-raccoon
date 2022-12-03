package com.raccoon.user;

import com.raccoon.dto.ProfileDto;
import com.raccoon.entity.Artist;
import com.raccoon.entity.RaccoonUser;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.factory.UserFactory;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.mail.RaccoonMailer;
import com.raccoon.search.dto.SearchResultArtistDto;
import com.raccoon.search.dto.mapping.ArtistMapper;
import com.raccoon.taste.lastfm.LastfmTasteUpdatingService;
import com.raccoon.user.dto.FollowedArtistDto;
import com.raccoon.user.dto.FollowedArtistsResponse;

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


    public List<Artist> getUserArtists(final RaccoonUser raccoonUser) {
        return userArtistRepository.findByUserIdSortedByWeight(raccoonUser.id)
                .stream()
                .map(UserArtist::getArtist)
                .toList();
    }

    /**
     * @param userEmail the raccoonUser of requesting followed Artists
     * @return
     */
    @NotNull
    public FollowedArtistsResponse getFollowedArtists(final String userEmail) {
        var user = userRepository.findByEmail(userEmail);

        List<FollowedArtistDto> rows = userArtistRepository.findByUserIdSortedByWeight(user.id)
                .stream()
                .map(UserArtist::getArtist)
                .map(artistMapper::toFollowedArtistDto)
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
        log.info("lastFmUsername: {}, isSpotifyEnabled: {}, showScrapeSpotifyButton: {}, showScrapeLastfmButton: {}",
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
     * Fetches the raccoonUser from the database. Sends welcome email blocking in case the raccoonUser was just created.
     * @param userEmail unique raccoonUser identifier
     * @return raccoonUser from the database.
     */
    public RaccoonUser completeRegistration(final String userEmail) {
        Optional<RaccoonUser> optionalUser = userRepository.findByEmailOptional(userEmail);

        return optionalUser.orElseGet(() -> {
            var user = userFactory.createUser(userEmail);
            mailer.sendWelcome(
                    user,
                    () -> {
                        log.info("Welcome sent to raccoonUser {}", user.id);
                        userRepository.persist(user);
                    },
                    () -> log.error("Something went wrong while sending welcome to {}", user.id)
            ).await().indefinitely();
            return user;
        });
    }

    /**
     * Create a new UserArtist association
     * @param userEmail raccoonUser requesting the follow
     * @param artistDto artistDto as it originates from an Artist search.
     */
    public void followArtist(final String userEmail, final SearchResultArtistDto artistDto) {
        artistFollowingService.followArtist(userEmail, artistMapper.fromDto(artistDto));
    }

    public void unfollowArtist(final String userEmail, final Long artistId) {
        artistFollowingService.unfollowArtist(userEmail, artistId);
    }

    public RaccoonUser enableTasteSources(final String userEmail,
                                          final Optional<String> lastfmUsernameOpt,
                                          final Optional<Boolean> enableSpotifyOpt) {
        Optional<RaccoonUser> existing = userRepository.findByEmailOptional(userEmail);
        if (existing.isEmpty()) {
            log.info("RaccoonUser does not exist.");
            throw new NotFoundException("RaccoonUser not found");
        }
        var user = existing.get();
        lastfmUsernameOpt.ifPresent(user::setLastfmUsername);
        enableSpotifyOpt.ifPresent(user::setSpotifyEnabled);

        lastfmTasteUpdatingService.updateTaste(user.id);

        userRepository.persist(user);
        return user;
    }

}
