package com.raccoon.search;

import com.raccoon.search.dto.ArtistSearchResponse;

import org.jboss.resteasy.annotations.jaxrs.QueryParam;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@ApplicationScoped
@Path("/artist")
public class ArtistSearchResource {

    final SearchService searchService;

    @Inject
    public ArtistSearchResource(final SearchService searchService) {
        this.searchService = searchService;
    }

    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    public ArtistSearchResponse searchArtists(@QueryParam String pattern,
                                              @QueryParam Optional<Integer> size) {
        return searchService.searchArtists(pattern, size);
    }

}
