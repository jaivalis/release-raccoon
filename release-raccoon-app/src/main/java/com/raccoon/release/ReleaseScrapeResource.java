package com.raccoon.release;

import com.raccoon.entity.Release;

import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/release-scrape")
public class ReleaseScrapeResource {

    @Inject
    ReleaseScrapeService service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Set<Release> scrapeReleases() throws InterruptedException {
        return service.scrapeReleases();
    }

}