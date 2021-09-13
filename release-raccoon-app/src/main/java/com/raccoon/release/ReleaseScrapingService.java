package com.raccoon.release;

import com.raccoon.entity.Release;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.exception.ReleaseScrapeException;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.UserTransaction;

import io.quarkus.scheduler.Scheduled;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class ReleaseScrapingService {

    ReleaseScrapers releaseScrapers;
    UserTransaction userTransaction;
    UserArtistRepository userArtistRepository;

    @Inject
    ReleaseScrapingService(final ReleaseScrapers releaseScrapers,
                           final UserTransaction userTransaction,
                           final UserArtistRepository userArtistRepository) {
        this.releaseScrapers = releaseScrapers;
        this.userTransaction = userTransaction;
        this.userArtistRepository = userArtistRepository;
    }

    @Scheduled(cron="{release.scrape.cron.expr}")
    public void releaseScrapeCronJob() throws ReleaseScrapeException, InterruptedException {
        log.info("Release scrape cronjob triggered");
        scrape();
    }

    public Set<Release> scrape() throws ReleaseScrapeException, InterruptedException {
        try {
            // Could optimize the txs by localizing and batching.
            userTransaction.setTransactionTimeout(3600);
            userTransaction.begin();
            Set<Release> releases = releaseScrapers.scrape();
            updateHasNewRelease(releases);
            userTransaction.commit();

            return releases;
        } catch (InterruptedException e) {
            log.warn("Scrape interrupted.", e);
            throw e;
        } catch (Exception e) {
            throw new ReleaseScrapeException("Exception thrown while scraping releases.", e);
        }
    }

    private void updateHasNewRelease(Collection<Release> releases) {
        List<Long> artistIds = releases.stream()
                .flatMap(release ->
                        release.getArtists()
                                .stream()
                                .map(artist -> artist.id)
                ).toList();
        List<UserArtist> userArtists = userArtistRepository.markNewRelease(artistIds);
        log.info("Updated {} UserArtists.", userArtists.size());
    }

}
