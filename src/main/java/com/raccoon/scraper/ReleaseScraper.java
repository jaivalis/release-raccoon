package com.raccoon.scraper;

import com.raccoon.entity.Release;

import java.util.List;
import java.util.Optional;

public interface ReleaseScraper {

    List<Release> scrapeReleases(Optional<Integer> limit) throws ReleaseScrapeException;

    Optional<Release> processRelease(Object release);

}
