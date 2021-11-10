package com.raccoon.user;

import com.raccoon.registration.RegisteringService;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;

import java.net.URI;
import java.util.Optional;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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
    RegisteringService registeringService;
    @IdToken
    JsonWebToken idToken;

    @Inject
    public UserProfileResource(final UserProfileService userProfileService,
                               final RegisteringService registeringService) {
        this.userProfileService = userProfileService;
        this.registeringService = registeringService;
    }

    @GET
    @NoCache
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public Response registerCallback() {
        final String email = idToken.getClaim(EMAIL_CLAIM);
        registeringService.completeRegistration(email);

        return Response.ok(userProfileService.getTemplateInstance(email)).build();
    }

    @Path("/unfollow/{artistId}")
    @POST
    @NoCache
    @Transactional
    @Valid
    @Produces(MediaType.TEXT_HTML)
    public Response unfollowArtist(@NotNull @PathParam("artistId") Long artistId) {
        log.info("Unfollowing artist {}", artistId);
        final String email = idToken.getClaim(EMAIL_CLAIM);
        userProfileService.unfollowArtist(email, artistId);

        return Response.ok(userProfileService.getTemplateInstance(email)).build();
    }

    @GET
    @Path("/enableServices")
    @Transactional
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public Response enableTasteSources(@QueryParam("lastfmUsername") final Optional<String> lastfmUsernameOpt,
                                       @QueryParam("enableSpotify") final Optional<Boolean> enableSpotifyOpt) {
        final String email = idToken.getClaim(EMAIL_CLAIM);

        userProfileService.enableTasteSources(email, lastfmUsernameOpt, enableSpotifyOpt);

        return Response.temporaryRedirect(URI.create("/me")).build();
    }
}
