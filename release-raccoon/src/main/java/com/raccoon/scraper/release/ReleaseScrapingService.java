package com.raccoon.scraper.release;

import com.raccoon.entity.Release;
import com.raccoon.entity.UserArtist;
import com.raccoon.exception.ReleaseScrapeException;
import com.raccoon.scraper.ReleaseScrapers;
import lombok.val;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.UserTransaction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class ReleaseScrapingService {

    @Inject
    ReleaseScrapers releaseScrapers;

    @Inject
    UserTransaction userTransaction;

    public List<Release> scrape() throws ReleaseScrapeException {
        try {
            // Could optimize the txs by localizing and batching.
            userTransaction.setTransactionTimeout(3600);
            userTransaction.begin();
            List<Release> releases = new ArrayList<>();
            for (val scraper : releaseScrapers) {
                releases.addAll(scraper.scrapeReleases(Optional.empty()));
            }
            userTransaction.commit();

            updateHasNewRelease(releases);

            return releases;
        } catch (Exception e) {
           throw new ReleaseScrapeException("Exception thrown while scraping releases.", e);
        }
    }

    private void updateHasNewRelease(Collection<Release> releases) {
        Collection<Long> artistIds = releases.stream()
                .flatMap(release -> release.getArtists().stream().map(artist -> artist.id))
                .collect(Collectors.toList());
        UserArtist.markNewRelease(artistIds);
    }

}
