package com.raccoon.scraper.release;

import com.raccoon.entity.User;
import lombok.val;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Optional;

@ApplicationScoped
public class ReleaseScrapingService {

    @Inject
    ReleaseScrapers releaseScrapers;

    @Transactional
    public User scrape() {

        for (val scraper : releaseScrapers) {
            scraper.scrapeReleases(Optional.empty());
        }

        return null;
    }

}
