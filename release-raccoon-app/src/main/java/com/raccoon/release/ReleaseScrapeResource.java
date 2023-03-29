package com.raccoon.release;

import com.raccoon.release.dto.ReleaseScrapeResponse;

import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/release-scrape")
public class ReleaseScrapeResource {

    @Inject
    ReleaseScrapeService service;

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public ReleaseScrapeResponse scrapeReleases() throws ExecutionException, InterruptedException {
        return service.scrapeReleases();
    }

}
