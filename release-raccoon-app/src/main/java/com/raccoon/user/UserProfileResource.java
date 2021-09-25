package com.raccoon.user;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.quarkus.oidc.IdToken;
import io.quarkus.security.Authenticated;
import lombok.extern.slf4j.Slf4j;

@Path("/me")
@Slf4j
@Authenticated
public class UserProfileResource {

    @Inject
    UserProfileService service;
    @IdToken
    JsonWebToken idToken;

    @GET
    @NoCache
    @Produces(MediaType.TEXT_HTML)
    public Response registerCallback() {
        final String email = idToken.getClaim("email");
        return Response.ok(service.getTemplateInstance(email)).build();
    }

    @Path("/unfollow")
    @DELETE
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public Response unfollowArtist(@QueryParam("artistId") final Long artistId) {
        final String email = idToken.getClaim("email");
        service.unfollowArtist(email, artistId);
        return Response.noContent().build();
    }

}
