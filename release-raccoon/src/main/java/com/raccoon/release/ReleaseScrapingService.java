package com.raccoon.release;

import com.raccoon.entity.Release;
import com.raccoon.entity.UserArtist;
import com.raccoon.exception.ReleaseScrapeException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.UserTransaction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
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
            updateHasNewRelease(releases);
            userTransaction.commit();

            return releases;
        } catch (Exception e) {
           throw new ReleaseScrapeException("Exception thrown while scraping releases.", e);
        }
    }

    private void updateHasNewRelease(Collection<Release> releases) {
        Collection<Long> artistIds = releases.stream()
                .flatMap(release -> release.getArtists().stream().map(artist -> artist.id))
                .collect(Collectors.toList());
        List<UserArtist> userArtists = UserArtist.markNewRelease(artistIds);
        log.info("Updated {} `UserArtist`.", userArtists.size());
    }

}
