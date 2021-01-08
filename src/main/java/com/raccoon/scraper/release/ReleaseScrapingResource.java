package com.raccoon.scraper.release;

import com.raccoon.entity.Release;
import com.raccoon.scraper.ReleaseScrapeException;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/release-scrape")
public class ReleaseScrapingResource {

    @Inject
    ReleaseScrapingService service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Release> scrapeReleases() throws ReleaseScrapeException {
        return service.scrape();
    }

}