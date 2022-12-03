package com.raccoon.user;

import com.raccoon.search.dto.SearchResultArtistDto;
import com.raccoon.user.dto.FollowedArtistsResponse;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;

import java.net.URI;
import java.util.Optional;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.quarkus.oidc.IdToken;
import io.quarkus.security.Authenticated;
import lombok.extern.slf4j.Slf4j;

import static com.raccoon.Constants.EMAIL_CLAIM;

@Path("/me")
@Slf4j
@Authenticated
public class UserProfileResource {

    UserProfileService userProfileService;
    @IdToken
    JsonWebToken idToken;

    @Inject
    public UserProfileResource(final UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    /**
     * Also called by the oidc service to complete the raccoonUser registration.
     * @return The rendered raccoonUser profile qute-template
     */
    @GET
    @NoCache
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public Response registrationCallback() {
        final String email = idToken.getClaim(EMAIL_CLAIM);
        userProfileService.completeRegistration(email);

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
    @Transactional
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

}
