package com.raccoon.search;

import com.raccoon.search.dto.mapping.ArtistSearchResponse;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.quarkus.oidc.IdToken;
import io.quarkus.security.Authenticated;

import static com.raccoon.Constants.EMAIL_CLAIM;

@Path("/artist")
@ApplicationScoped
@Authenticated
public class ArtistSearchResource {

    final SearchService searchService;

    @IdToken
    JsonWebToken idToken;

    @Inject
    public ArtistSearchResource(final SearchService searchService) {
        this.searchService = searchService;
    }

    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    public ArtistSearchResponse searchArtists(@QueryParam String pattern,
                                              @QueryParam Optional<Integer> size) {
        final String email = idToken.getClaim(EMAIL_CLAIM);
        return searchService.searchArtists(email, pattern, size);
    }

}
