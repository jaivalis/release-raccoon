package com.raccoon.scrape;

import com.raccoon.entity.Scrape;
import com.raccoon.entity.repository.ScrapeRepository;
import com.raccoon.scrape.dto.ReleaseScrapeResponse;

import java.time.LocalDateTime;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class ReleaseScrapeService {

    final ScrapeRepository scrapeRepository;
    final ReleaseScrapeWorker worker;

    @Inject
    ReleaseScrapeService(final ScrapeRepository scrapeRepository, final ReleaseScrapeWorker worker) {
        this.scrapeRepository = scrapeRepository;
        this.worker = worker;
    }

    @Transactional
    public ReleaseScrapeResponse scrapeReleases() {
        final Scrape scrape;

        var daysSinceLastAllowedScrape = LocalDateTime.now().minusDays(1);
        Optional<Scrape> mostRecentScrape = scrapeRepository.getMostRecentScrapeFrom(daysSinceLastAllowedScrape);
        if (mostRecentScrape.isPresent()) {
            log.info("No scrape is needed, latest scrape took place on: {}", mostRecentScrape.get().getCompleteDate());
            scrape = mostRecentScrape.get();
        } else {
            if (!worker.isRunning()) {
                worker.submit();
            }
            scrape = worker.getLatestScrape();
        }

        return new ReleaseScrapeResponse(scrape);
    }

}
