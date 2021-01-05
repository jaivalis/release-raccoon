package com.raccoon.scraper.taste;

import com.raccoon.scraper.LastfmScraper;
import com.raccoon.scraper.TasteScraper;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

@ApplicationScoped
public class TasteScrapers implements Iterable<TasteScraper> {

    @Inject
    LastfmScraper lastfmScraper;

    List<TasteScraper> scrapers;

    public TasteScrapers(LastfmScraper lastfmScraper) {
        this.lastfmScraper = lastfmScraper;
        scrapers = List.of(lastfmScraper);
    }

    @Override
    public Iterator<TasteScraper> iterator() {
        return scrapers.iterator();
    }

    @Override
    public void forEach(Consumer<? super TasteScraper> action) {
        scrapers.forEach(action);
    }

    @Override
    public Spliterator<TasteScraper> spliterator() {
        return scrapers.spliterator();
    }
}
