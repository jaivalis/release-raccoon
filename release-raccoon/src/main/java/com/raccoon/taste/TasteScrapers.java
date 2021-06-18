package com.raccoon.taste;

import com.raccoon.scraper.LastfmScraper;
import com.raccoon.scraper.TasteScraper;
import com.raccoon.scraper.spotify.SpotifyScraper;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

@ApplicationScoped
public class TasteScrapers implements Iterable<TasteScraper> {

//    @Inject
//    Instance<LastfmScraper> lastfmScraper;
    @Inject
    Instance<SpotifyScraper> spotifyScraper;

    List<TasteScraper> scrapers = new ArrayList<>();

    @PostConstruct
    private void init() {
//        scrapers.add(lastfmScraper.get());
        scrapers.add(spotifyScraper.get());
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
