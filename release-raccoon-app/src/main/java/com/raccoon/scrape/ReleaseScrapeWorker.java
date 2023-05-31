package com.raccoon.scrape;

import com.raccoon.entity.Artist;
import com.raccoon.entity.Release;
import com.raccoon.entity.Scrape;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.repository.ScrapeRepository;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.scrape.dto.ReleaseMapper;
import com.raccoon.scraper.ReleaseScraper;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.raccoon.common.StringUtil.isNullOrEmpty;

@Slf4j
@ApplicationScoped
public class ReleaseScrapeWorker {

    final List<ReleaseScraper<?>> releaseScrapers;
    final UserArtistRepository userArtistRepository;
    final ScrapeRepository scrapeRepository;
    final ReleaseMapper releaseMapper;

    final AtomicBoolean isRunning = new AtomicBoolean(false);
    final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private Scrape latestScrape;

    @Inject
    ReleaseScrapeWorker(final Instance<ReleaseScraper<?>> releaseScrapers,
                        final UserArtistRepository userArtistRepository,
                        final ScrapeRepository scrapeRepository,
                        final ReleaseMapper releaseMapper) {
        this.releaseScrapers = releaseScrapers.stream().toList();
        log.info("Found {} release scrapers in classpath", this.releaseScrapers.size());
        this.userArtistRepository = userArtistRepository;
        this.scrapeRepository = scrapeRepository;
        this.releaseMapper = releaseMapper;
    }

    /**
     * Submits a scrape job. If one is active and incomplete, returns that one instead.
     */
    public void submit() {
        log.info("Starting new scrape job");
        isRunning.set(true);
        latestScrape = new Scrape();

        executorService.submit(() -> {
            try {
                Set<Release> releases = fetchReleases();
                persistLatestScrape(releases);
            } catch (PersistenceException ex) {
                log.error("Could not persist latest scrape ", ex);
                throw ex;
            } finally {
                log.info("Scrape job complete");
                isRunning.set(false);
            }
        });
    }

    public Scrape getLatestScrape() {
        return latestScrape;
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    Set<Release> fetchReleases() {
        Set<Release> releases = new HashSet<>();
        for (ReleaseScraper<?> scraper : releaseScrapers) {
            scrapeReleases(releases, scraper);
        }
        log.info("Total new releases found: {}", releases.size());

        return releases;
    }

    /**
     * Will persist new releases and mark artists that need to be notified
     * @param releases
     */
    @Transactional
    void persistLatestScrape(Collection<Release> releases) {
        latestScrape.setReleasesFromSpotify(
                releases.stream()
                        .filter(r -> !isNullOrEmpty(r.getSpotifyUriId()))
                        .count()
        );
        latestScrape.setReleasesFromMusicbrainz(
                releases.stream()
                        .filter(r -> !isNullOrEmpty(r.getMusicbrainzId()))
                        .count()
        );
        latestScrape.setReleaseCount(releases.size());
        latestScrape.setIsComplete(true);
        latestScrape.setCompleteDate(LocalDateTime.now());
        latestScrape.getReleases().addAll(releases);
        scrapeRepository.persist(latestScrape);
        updateHasNewRelease(releases);
    }

    /**
     * Mark artists that need to be notified
     * @param releases need
     */
    void updateHasNewRelease(Collection<Release> releases) {
        List<Long> artistIds = releases.stream()
                .flatMap(release ->
                        release.getArtists()
                                .stream()
                                .map(Artist::getId)
                ).toList();
        List<UserArtist> userArtists = userArtistRepository.markNewRelease(artistIds);
        log.info("Updated {} UserArtist(s) of new releases", userArtists);
    }

    void scrapeReleases(Set<Release> releases, ReleaseScraper<?> scraper) {
        try {
            final Set<Release> releasesPerScraper = scraper.scrapeReleases(Optional.empty());
            releases.addAll(releasesPerScraper);
            log.info("New releases found through {}: {}", scraper.getClass().getSimpleName(), releasesPerScraper.size());
        } catch (InterruptedException e) {
            log.warn("Thread was interrupted while scraping releases");
            Thread.currentThread().interrupt();
        }
    }

}
