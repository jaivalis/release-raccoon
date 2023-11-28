package com.raccoon.scrape;

import com.raccoon.scrape.dto.ReleaseScrapeResponse;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class ReleaseScrapeScheduler {

    final ReleaseScrapeService service;

    @Inject
    ReleaseScrapeScheduler(ReleaseScrapeService service) {
        this.service = service;
    }

    @Scheduled(cron="{release.scrape.cron.expr}")
    public void releaseScrapeCronJob() {
        log.info("Release scrape cronjob triggered");
        ReleaseScrapeResponse scrape = service.scrapeReleases();
        log.info("Scrape complete: {}", scrape);
    }

}
