package com.raccoon.user;

import com.raccoon.search.dto.SearchResultArtistDto;
import com.raccoon.user.dto.FollowedArtistsResponse;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;

import java.net.URI;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import io.quarkus.oidc.IdToken;
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

import static com.raccoon.Constants.EMAIL_CLAIM;

@Path("/me")
@Slf4j
@Authenticated
public class UserProfileResource {

    UserProfileService userProfileService;
    RedirectConfig redirectConfig;
    @IdToken
    JsonWebToken idToken;

    @Inject
    public UserProfileResource(final UserProfileService userProfileService, final RedirectConfig redirectConfig) {
        this.userProfileService = userProfileService;
        this.redirectConfig = redirectConfig;
    }

    /**
     * Also called by the oidc service to complete the raccoonUser registration.
     * @return The rendered raccoonUser profile qute-template
     */
    @GET
    @NoCache
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public Response registrationCallback(@QueryParam("redirectUrl") String redirectUrl) {
        final String email = idToken.getClaim(EMAIL_CLAIM);
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
        final String email = idToken.getClaim(EMAIL_CLAIM);

        userProfileService.followArtist(email, artistDto);

        return Response.noContent().build();
    }

    @Path("/unfollow/{artistId}")
    @DELETE
    @NoCache
    @Transactional
    @Valid
    public Response unfollowArtist(@NotNull @PathParam("artistId") Long artistId) {
        log.info("Unfollowing artist {}", artistId);
        final String email = idToken.getClaim(EMAIL_CLAIM);
        userProfileService.unfollowArtist(email, artistId);

        return Response.noContent().build();
    }

    @GET
    @Path("/enable-services")
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public Response enableTasteSources(@QueryParam("lastfmUsername") final Optional<String> lastfmUsernameOpt,
                                       @QueryParam("enableSpotify") final Optional<Boolean> enableSpotifyOpt) {
        final String email = idToken.getClaim(EMAIL_CLAIM);

        userProfileService.enableTasteSources(email, lastfmUsernameOpt, enableSpotifyOpt);

        return Response.temporaryRedirect(URI.create("/me")).build();
    }

    @GET
    @Path("/followed-artists")
    @Transactional
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public FollowedArtistsResponse getFollowedArtists() {
        final String email = idToken.getClaim(EMAIL_CLAIM);

        return userProfileService.getFollowedArtists(email);
    }

    private boolean shouldRedirect(String redirectUrl) {
        return Objects.nonNull(redirectUrl)
                && Objects.nonNull(redirectConfig.getWhitelistedUrls())
                && redirectConfig.getWhitelistedUrls().orElse(Collections.emptyList()).contains(redirectUrl);
    }

}
