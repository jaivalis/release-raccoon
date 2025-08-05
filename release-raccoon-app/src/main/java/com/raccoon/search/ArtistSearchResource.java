package com.raccoon.search;

import com.raccoon.search.dto.mapping.ArtistSearchResponse;

import org.jboss.resteasy.reactive.RestQuery;

import java.util.Optional;

import io.quarkus.oidc.UserInfo;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/artist")
@ApplicationScoped
@Authenticated
public class ArtistSearchResource {

    final SearchService searchService;
    final UserInfo userInfo;

    @Inject
    public ArtistSearchResource(final SearchService searchService,
                                final UserInfo userInfo) {
        this.searchService = searchService;
        this.userInfo = userInfo;
    }

    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    public ArtistSearchResponse searchArtists(@RestQuery String pattern,
                                              @RestQuery Optional<Integer> size) {
        final String email = userInfo.getEmail();
        return searchService.searchArtists(email, pattern, size);
    }

}
