package com.raccoon.release;

import com.raccoon.entity.Release;

import java.util.Set;

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
    public void releaseScrapeCronJob() throws InterruptedException {
        log.info("Release scrape cronjob triggered");
        Set<Release> releases = service.scrapeReleases();
        service.updateHasNewRelease(releases);
    }

}
