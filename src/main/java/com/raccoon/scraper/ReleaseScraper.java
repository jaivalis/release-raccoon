package com.raccoon.scraper;

import com.raccoon.entity.Release;
import com.raccoon.exception.ReleaseScrapeException;

import java.util.List;
import java.util.Optional;

public interface ReleaseScraper {

    List<Release> scrapeReleases(Optional<Integer> limit) throws ReleaseScrapeException, InterruptedException;

    Optional<Release> processRelease(Object release);

}
