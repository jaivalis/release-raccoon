package com.raccoon.ui.integration;

import org.eclipse.microprofile.jwt.JsonWebToken;

import io.quarkus.oidc.IdToken;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/test-ui")
@Authenticated
public class TestResource {

    @Inject
    SecurityIdentity securityIdentity; // This works for both flows

    @Inject
    JsonWebToken accessToken; // For bearer token flow

    @Inject
    @IdToken
    JsonWebToken idToken; // For web flow (optional)

    @GET
    @Path("/test-ui")
    @Authenticated
    public Response getRecommended() {
        // Log which authentication type we're dealing with
        log.info("Authentication type: " + securityIdentity.getClass().getName());

        // Use SecurityIdentity which works for both flows
        String principal = securityIdentity.getPrincipal().getName();
        log.info("Principal name: " + principal);
        // Your existing logic here

        log.info("User info: " + getUserInfo());

        return Response.ok().build();
    }

    private String getUserInfo() {
        if (idToken != null) {
            log.info("idToken: " + idToken);
            idToken.getClaim("email");
        }
        if (accessToken != null) {
            log.info("Access token: " + accessToken);
            return accessToken.getClaim("email");
        }
        return null;
    }
}
