package com.raccoon.scraper.release;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/release-scrape")
public class ReleaseScrapingResource {

    @Inject
    ReleaseScrapingService service;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String register() {
        service.scrape();
        return "Scraped (?)";
    }

}