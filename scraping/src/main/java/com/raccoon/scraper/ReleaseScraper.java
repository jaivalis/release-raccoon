package com.raccoon.scraper;

import com.raccoon.entity.Release;

import java.util.Optional;
import java.util.Set;

public interface ReleaseScraper {

    Set<Release> scrapeReleases(Optional<Integer> limit) throws InterruptedException;

    Optional<Release> processRelease(Object release);

}
