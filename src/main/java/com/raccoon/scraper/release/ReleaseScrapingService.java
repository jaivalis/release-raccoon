package com.raccoon.scraper.release;

import com.raccoon.entity.Release;
import com.raccoon.exception.ReleaseScrapeException;
import lombok.val;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ReleaseScrapingService {

    @Inject
    ReleaseScrapers releaseScrapers;

    @Inject
    UserTransaction userTransaction;

    public List<Release> scrape() throws ReleaseScrapeException, InterruptedException, SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException {
        // Could optimize the txs by localizing and batching.
        userTransaction.setTransactionTimeout(3600);
        userTransaction.begin();
        List<Release> releases = new ArrayList<>();
        for (val scraper : releaseScrapers) {
            releases.addAll(scraper.scrapeReleases(Optional.empty()));
        }
        userTransaction.commit();
        return releases;
    }

}
