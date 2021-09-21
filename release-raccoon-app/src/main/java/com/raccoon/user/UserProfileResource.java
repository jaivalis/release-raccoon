package com.raccoon.user;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.annotations.cache.NoCache;

import javax.inject.Inject;
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
}
