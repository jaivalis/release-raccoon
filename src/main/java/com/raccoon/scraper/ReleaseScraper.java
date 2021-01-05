package com.raccoon.scraper;

import java.util.Optional;

public interface ReleaseScraper {

    void scrapeReleases(Optional<Integer> limit);

    void processRelease(Object release);

}
