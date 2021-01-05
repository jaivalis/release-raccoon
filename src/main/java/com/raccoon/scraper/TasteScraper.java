package com.raccoon.scraper;

import java.util.Optional;

public interface TasteScraper {

    void scrapeTaste(final String username, Optional<Integer> limit);

    void processArtist(Object entry);

}
