package com.raccoon.user;

import com.raccoon.search.dto.SearchResultArtistDto;
import com.raccoon.user.dto.FollowedArtistsReleaseResponse;
import com.raccoon.user.dto.FollowedArtistsResponse;
import com.raccoon.user.dto.UserProfile;

import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.reactive.NoCache;
import org.jboss.resteasy.reactive.RestQuery;

import java.net.URI;
import java.util.Collections;
import java.util.Optional;

import io.quarkus.oidc.UserInfo;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Path("/me")
@Slf4j
@Authenticated
public class UserProfileResource {

    final UserProfileService userProfileService;
    final RedirectConfig redirectConfig;
    final UserInfo userInfo;

    @Inject
    public UserProfileResource(final UserProfileService userProfileService,
                               final RedirectConfig redirectConfig,
                               final UserInfo userInfo) {
        this.userProfileService = userProfileService;
        this.redirectConfig = redirectConfig;
        this.userInfo = userInfo;
    }

    /**
     * Also called by the oidc service to complete the raccoonUser registration.
     * @return The rendered raccoonUser profile qute-template
     */
    @GET
    @NoCache
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public Response registrationCallback(@RestQuery("redirectUrl") String redirectUrl) {
        final String email = userInfo.getEmail();
        userProfileService.completeRegistration(email);
        if (shouldRedirect(redirectUrl)) {
            log.info("Redirecting to ");
            return Response.temporaryRedirect(URI.create(redirectUrl)).build();
        }

        return Response.ok(userProfileService.renderTemplateInstance(email)).build();
    }

    @Path("/follow")
    @POST
    @NoCache
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    public Response followArtist(@Valid @NotNull SearchResultArtistDto artistDto) {
        log.info("Following artist {}", artistDto);
        final String email = userInfo.getEmail();

        userProfileService.followArtist(email, artistDto);

        return Response.noContent().build();
    }

    @Path("/unfollow/{artistId}")
    @DELETE
    @NoCache
    @Transactional
    @Valid
    public Response unfollowArtist(@NotNull @PathParam("artistId") String artistId) {
        log.info("Unfollowing artist {}", artistId);
        final String email = userInfo.getEmail();
        if (!StringUtils.isBlank(artistId)) {
            var artistIdLong = Long.parseLong(artistId);
            userProfileService.unfollowArtist(email, artistIdLong);
        } else {
            log.warn("Artist id {} is not a valid long, ignoring unfollow request.", artistId);
        }
        return Response.noContent().build();
    }

    @GET
    @Path("/profile")
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public UserProfile getUserProfile() {
        final String email = userInfo.getEmail();
        return userProfileService.getUserProfile(email);
    }

    @GET
    @Path("/enable-services")
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public Response enableTasteSources(@RestQuery("lastfmUsername") final Optional<String> lastfmUsernameOpt,
                                       @RestQuery("enableSpotify") final Optional<Boolean> enableSpotifyOpt) {
        final String email = userInfo.getEmail();

        userProfileService.enableTasteSources(email, lastfmUsernameOpt, enableSpotifyOpt);

        return Response.temporaryRedirect(URI.create("/me")).build();
    }

    @GET
    @Path("/followed-artists")
    @Transactional
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public FollowedArtistsResponse getFollowedArtists(@RestQuery Optional<Integer> page,
                                                      @RestQuery Optional<Integer> size) {
        final String email = userInfo.getEmail();

        return userProfileService.getFollowedArtists(email, page, size);
    }

    @GET
    @Path("/followed-artists/releases")
    @Transactional
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public FollowedArtistsReleaseResponse getFollowedArtistsReleases(@RestQuery Optional<Integer> days) {
        final String email = userInfo.getEmail();

        return userProfileService.getFollowedArtistsReleases(email, days.orElse(30));
    }

    private boolean shouldRedirect(String redirectUrl) {
        log.debug("Should redirect whitelist {}", isNull(redirectConfig) ? "null" : redirectConfig.whitelistedUrls());
        return nonNull(redirectUrl)
                && nonNull(redirectConfig.whitelistedUrls())
                && redirectConfig.whitelistedUrls().orElse(Collections.emptyList()).contains(redirectUrl);
    }

}
