package com.raccoon.release;

import com.raccoon.entity.Release;
import com.raccoon.scraper.ReleaseScraper;
import com.raccoon.scraper.spotify.SpotifyScraper;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;

import lombok.val;

@ApplicationScoped
public class ReleaseScrapers implements Iterable<ReleaseScraper> {

    @Inject
    SpotifyScraper spotifyScraper;

    List<ReleaseScraper> scrapers;

    public ReleaseScrapers(SpotifyScraper lastfmScraper) {
        this.spotifyScraper = lastfmScraper;
        scrapers = List.of(lastfmScraper);
    }

    public Set<Release> scrape() throws IOException, InterruptedException {
        Set<Release> releases = new HashSet<>();
        for (val scraper : scrapers) {
            // smart merging is necessary here if more scrapers are added.
            releases.addAll(scraper.scrapeReleases(Optional.empty()));
        }
        return releases;
    }

    @Override
    public Iterator<ReleaseScraper> iterator() {
        return scrapers.iterator();
    }

    @Override
    public void forEach(Consumer<? super ReleaseScraper> action) {
        scrapers.forEach(action);
    }

    @Override
    public Spliterator<ReleaseScraper> spliterator() {
        return scrapers.spliterator();
    }
}
