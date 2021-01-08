package com.raccoon.scraper.release;

import com.raccoon.entity.Release;
import com.raccoon.scraper.ReleaseScrapeException;
import lombok.val;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ReleaseScrapingService {

    @Inject
    ReleaseScrapers releaseScrapers;

    @Transactional
    public List<Release> scrape() throws ReleaseScrapeException {
        List<Release> releases = new ArrayList<>();

        for (val scraper : releaseScrapers) {
            releases.addAll(scraper.scrapeReleases(Optional.empty()));
        }

        return releases;
    }

}
