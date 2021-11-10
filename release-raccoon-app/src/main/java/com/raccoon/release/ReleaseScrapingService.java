package com.raccoon.release;

import com.raccoon.dto.ReleaseScrapeResponse;
import com.raccoon.entity.Release;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.exception.ReleaseScrapeException;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.quarkus.scheduler.Scheduled;
import lombok.extern.slf4j.Slf4j;

import static com.raccoon.Constants.RELEASE_SCRAPE_RESPONSE_FAILURE_TEMPLATE;
import static com.raccoon.Constants.RELEASE_SCRAPE_RESPONSE_SUCCESS_TEMPLATE;

@Slf4j
@ApplicationScoped
public class ReleaseScrapingService {

    ReleaseScrapers releaseScrapers;
    UserArtistRepository userArtistRepository;

    @Inject
    ReleaseScrapingService(final ReleaseScrapers releaseScrapers,
                           final UserArtistRepository userArtistRepository) {
        this.releaseScrapers = releaseScrapers;
        this.userArtistRepository = userArtistRepository;
    }

    @Scheduled(cron="{release.scrape.cron.expr}")
    public void releaseScrapeCronJob() throws ReleaseScrapeException, InterruptedException {
        log.info("Release scrape cronjob triggered");
        scrape();
    }

    public ReleaseScrapeResponse scrape() throws ReleaseScrapeException, InterruptedException {
        try {
            // Could optimize the txs by localizing and batching.
            Set<Release> releases = releaseScrapers.scrape();
            updateHasNewRelease(releases);

            return new ReleaseScrapeResponse(
                    Boolean.TRUE,
                    String.format(RELEASE_SCRAPE_RESPONSE_SUCCESS_TEMPLATE, releases.size())
            );
        } catch (InterruptedException e) {
            log.warn("Scrape interrupted.", e);
            throw e;
        } catch (Exception e) {
            return new ReleaseScrapeResponse(
                    Boolean.FALSE,
                    String.format(RELEASE_SCRAPE_RESPONSE_FAILURE_TEMPLATE, e.getMessage())
            );
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
