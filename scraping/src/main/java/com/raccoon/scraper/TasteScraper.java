package com.raccoon.scraper;

import com.raccoon.entity.Artist;

import org.apache.commons.lang3.tuple.MutablePair;

import java.util.Collection;
import java.util.Optional;

public interface TasteScraper {

    Collection<MutablePair<Artist, Float>> scrapeTaste(final String username, final Optional<Integer> limit);

    Artist processArtist(final Object entry);

}
