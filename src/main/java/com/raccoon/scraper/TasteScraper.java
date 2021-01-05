package com.raccoon.scraper;

import com.raccoon.entity.Artist;
import org.apache.commons.lang3.tuple.MutablePair;
import org.graalvm.collections.Pair;

import java.util.Collection;
import java.util.Optional;

public interface TasteScraper {

    Collection<MutablePair<Artist, Float>> scrapeTaste(final String username, Optional<Integer> limit);

    Artist processArtist(Object entry);

}
