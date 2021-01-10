package com.raccoon.scraper;

import com.raccoon.entity.Release;

import java.io.IOError;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface ReleaseScraper {

    List<Release> scrapeReleases(Optional<Integer> limit) throws IOException, InterruptedException;

    Optional<Release> processRelease(Object release);

}
