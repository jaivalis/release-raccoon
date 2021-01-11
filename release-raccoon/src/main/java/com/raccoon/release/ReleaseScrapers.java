package com.raccoon.release;

import com.raccoon.scraper.ReleaseScraper;
import com.raccoon.scraper.SpotifyScraper;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

@ApplicationScoped
public class ReleaseScrapers implements Iterable<ReleaseScraper> {

    @Inject
    SpotifyScraper spotifyScraper;

    List<ReleaseScraper> scrapers;

    public ReleaseScrapers(SpotifyScraper lastfmScraper) {
        this.spotifyScraper = lastfmScraper;
        scrapers = List.of(lastfmScraper);
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
