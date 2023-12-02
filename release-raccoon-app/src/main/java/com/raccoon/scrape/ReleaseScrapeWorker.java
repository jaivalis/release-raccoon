package com.raccoon.scrape;

import com.raccoon.entity.Artist;
import com.raccoon.entity.Release;
import com.raccoon.entity.Scrape;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.repository.ScrapeRepository;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.scrape.dto.ReleaseMapper;
import com.raccoon.scraper.ReleaseScraper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.quarkus.runtime.Shutdown;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

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

    @Getter
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

    @Shutdown
    void onStop() {
        executorService.shutdownNow();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }

//    /**
//     * Submits a scrape job. If one is active and incomplete, returns that one instead.
//     */
//    public void submitScrapeJobAsyncOld() {
//        log.info("Starting new scrape job");
//        latestScrape = new Scrape();
//
//        executorService.submit(() -> {
//            isRunning.set(true);
//            final Thread currentThread = Thread.currentThread();
//            currentThread.setName("processing-latest-scrape");
//            try {
//                Set<Release> releases = fetchReleases();
//                log.info("Scraped {} releases", releases.size());
//                persistLatestScrape(releases);
//                log.info("Scrape persisted");
//            } catch (PersistenceException ex) {
//                log.error("Could not persist latest scrape ", ex);
//                throw ex;
//            } catch (Exception e) {
//                log.error("Exception while fetching releases ", e);
//            } finally {
//                log.info("Scrape job complete");
//                isRunning.set(false);
//            }
//        });
//    }

    /**
     * Submits a scrape job
     */
    public void submitScrapeJobAsync() {
        log.info("Starting new scrape job");
        latestScrape = new Scrape();

        CompletableFuture.runAsync(scrapeRunnable(), executorService);
    }

    private Runnable scrapeRunnable() {
        return () -> {
            isRunning.set(true);
            final Thread currentThread = Thread.currentThread();
            currentThread.setName("processing-latest-scrape");
            try {
                Set<Release> releases = fetchReleases();
                log.info("Scraped {} releases", releases.size());
                persistLatestScrape(releases);
                log.info("Scrape persisted");
            } catch (PersistenceException ex) {
                log.error("Could not persist latest scrape ", ex);
                throw ex;
            } catch (Exception e) {
                log.error("Exception while fetching releases ", e);
            } finally {
                log.info("Scrape job complete");
                isRunning.set(false);
            }
        };
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    Set<Release> fetchReleases() {
        Set<Release> releases = new HashSet<>();
        for (ReleaseScraper<?> scraper : releaseScrapers) {
            log.debug("Scraping using {}", scraper.getClass().getSimpleName());
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
        log.info("Persisting {} releases", releases.size());
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
