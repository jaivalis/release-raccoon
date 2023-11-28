package com.raccoon.scrape;

import com.raccoon.scrape.dto.ReleaseScrapeResponse;

import jakarta.inject.Inject;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/release-scrape")
public class ReleaseScrapeResource {

    @Inject
    ReleaseScrapeService service;

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public ReleaseScrapeResponse scrapeReleases() {
        return service.scrapeReleases();
    }

}
