package com.raccoon.release;

import com.raccoon.release.dto.ReleaseScrapeResponse;

import java.util.concurrent.ExecutionException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.quarkus.scheduler.Scheduled;
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
    public void releaseScrapeCronJob() throws InterruptedException, ExecutionException {
        log.info("Release scrape cronjob triggered");
        ReleaseScrapeResponse scrape = service.scrapeReleases();
        log.info("Scrape complete: {}", scrape);
    }

}
