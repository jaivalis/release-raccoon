package com.raccoon.release;

import com.raccoon.entity.Release;
import com.raccoon.exception.ReleaseScrapeException;

import java.util.Set;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/release-scrape")
public class ReleaseScrapingResource {

    @Inject
    ReleaseScrapingService service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Set<Release> scrapeReleases() throws InterruptedException, ReleaseScrapeException {
        return service.scrape();
    }

}