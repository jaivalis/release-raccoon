package com.raccoon.user;

import com.raccoon.dto.ArtistDto;
import com.raccoon.dto.ProfileDto;
import com.raccoon.dto.mapping.ArtistMapper;
import com.raccoon.entity.Artist;
import com.raccoon.entity.RaccoonUser;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.factory.UserFactory;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.mail.RaccoonMailer;
import com.raccoon.search.dto.SearchResultArtistDto;
import com.raccoon.taste.lastfm.LastfmTasteUpdatingService;
import com.raccoon.user.dto.FollowedArtistsResponse;
import com.raccoon.user.dto.UserProfile;
import com.raccoon.user.settings.UserSettingsService;
import com.raccoon.user.settings.dto.UserSettingsDto;

import java.util.List;
import java.util.Optional;

import io.netty.util.internal.StringUtil;
import io.quarkus.panache.common.Page;
import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.NotFoundException;
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
    private final UserSettingsService userSettingsService;

    @Inject
    public UserProfileService(final UserRepository userRepository,
                              final UserFactory userFactory,
                              final UserArtistRepository userArtistRepository,
                              final LastfmTasteUpdatingService lastfmTasteUpdatingService,
                              final RaccoonMailer mailer,
                              final Engine engine,
                              final ArtistFollowingService artistFollowingService,
                              final ArtistMapper artistMapper,
                              final UserSettingsService userSettingsService) {
        this.userRepository = userRepository;
        this.userFactory = userFactory;
        this.userArtistRepository = userArtistRepository;
        this.lastfmTasteUpdatingService = lastfmTasteUpdatingService;
        this.mailer = mailer;
        this.profile = engine.getTemplate(PROFILE_TEMPLATE_ID);
        this.artistFollowingService = artistFollowingService;
        this.artistMapper = artistMapper;
        this.userSettingsService = userSettingsService;
    }

    public List<Artist> getUserArtists(final RaccoonUser raccoonUser) {
        return userArtistRepository.findByUserIdSortedByWeight(raccoonUser.id)
                .stream()
                .map(UserArtist::getArtist)
                .toList();
    }

    /**
     * @param userEmail the raccoonUser of requesting followed Artists
     * @return all followed artists (backwards compatible)
     */
    @NotNull
    public FollowedArtistsResponse getFollowedArtists(final String userEmail) {
        return getFollowedArtists(userEmail, Optional.empty(), Optional.empty());
    }

    /**
     * @param userEmail the raccoonUser of requesting followed Artists
     * @param page optional page number (0-based, defaults to 0)
     * @param size optional page size (defaults to all results)
     * @return paginated followed artists
     */
    @NotNull
    public FollowedArtistsResponse getFollowedArtists(final String userEmail, 
                                                     final Optional<Integer> page, 
                                                     final Optional<Integer> size) {
        var user = userRepository.findByEmail(userEmail);
        
        // If no pagination params provided, return all results (backwards compatibility)
        if (page.isEmpty() && size.isEmpty()) {
            List<ArtistDto> rows = userArtistRepository.findByUserIdSortedByWeight(user.id)
                    .stream()
                    .map(UserArtist::getArtist)
                    .map(artist -> {
                        ArtistDto dto = artistMapper.toArtistDto(artist);
                        dto.setFollowerCount(artist.getFollowerCount());
                        return dto;
                    })
                    .toList();
            return FollowedArtistsResponse.builder()
                    .rows(rows)
                    .total(rows.size())
                    .build();
        }
        
        // Use pagination
        int pageNumber = page.orElse(0);
        int pageSize = size.orElse(10); // Default page size
        Page pageRequest = Page.of(pageNumber, pageSize);
        
        List<UserArtist> pagedUserArtists = userArtistRepository.findByUserIdSortedByWeight(user.id, pageRequest);
        long totalCount = userArtistRepository.countByUserId(user.id);
        
        List<ArtistDto> rows = pagedUserArtists.stream()
                .map(UserArtist::getArtist)
                .map(artistMapper::toArtistDto)
                .toList();
                
        return FollowedArtistsResponse.builder()
                .rows(rows)
                .total((int) totalCount)
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


    @Transactional
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
        userRepository.persist(user);

        lastfmTasteUpdatingService.updateTaste(user.id);
        userRepository.persist(user);

        return user;
    }

    public UserProfile getUserProfile(String userEmail) {
        Optional<RaccoonUser> existing = userRepository.findByEmailOptional(userEmail);
        if (existing.isEmpty()) {
            log.info("RaccoonUser does not exist.");
            throw new NotFoundException("RaccoonUser not found");
        }
        var user = existing.get();

        UserSettingsDto userSettings = userSettingsService.getUserSettings(userEmail);

        return new UserProfile(
                user.getId().toString(),
                Boolean.TRUE.equals(user.getSpotifyEnabled()),
                user.getLastfmUsername(),
                userSettings.getUnsubscribed(),
                userSettings.getNotifyIntervalDays()
        );
    }
}
