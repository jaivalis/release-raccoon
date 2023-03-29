package com.raccoon.release;

import com.raccoon.entity.Artist;
import com.raccoon.entity.Release;
import com.raccoon.entity.Scrape;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.repository.ScrapeRepository;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.release.dto.ReleaseMapper;
import com.raccoon.release.dto.ReleaseScrapeResponse;
import com.raccoon.scraper.ReleaseScraper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.transaction.Transactional;

import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

import static com.raccoon.common.StringUtil.isNullOrEmpty;

@Slf4j
@ApplicationScoped
public class ReleaseScrapeService {

    final List<ReleaseScraper<?>> releaseScrapers;
    final UserArtistRepository userArtistRepository;
    final ScrapeRepository scrapeRepository;
    final ReleaseMapper releaseMapper;

    @Inject
    ReleaseScrapeService(final Instance<ReleaseScraper<?>> releaseScrapers,
                         final UserArtistRepository userArtistRepository,
                         final ScrapeRepository scrapeRepository,
                         final ReleaseMapper releaseMapper) {
        this.releaseScrapers = releaseScrapers.stream().toList();
        log.info("Found {} release scrapers in classpath", this.releaseScrapers.size());
        this.userArtistRepository = userArtistRepository;
        this.scrapeRepository = scrapeRepository;
        this.releaseMapper = releaseMapper;
    }

    @Transactional
    public ReleaseScrapeResponse scrapeReleases() throws ExecutionException, InterruptedException {
        Scrape scrape;
        ReleaseScrapeResponse response;

        var daysSinceLastAllowedScrape = LocalDateTime.now().minusDays(1);
        Optional<Scrape> mostRecentScrape = scrapeRepository.getMostRecentScrapeFrom(daysSinceLastAllowedScrape);
        if (mostRecentScrape.isPresent()) {
            log.info("No scrape is needed, latest scrape took place on: {}", mostRecentScrape.get().getCompleteDate());
            scrape = mostRecentScrape.get();
            response = new ReleaseScrapeResponse(scrape, null);

        } else {
            log.info("Starting new scrape");
            scrape = new Scrape();
            scrapeRepository.persist(scrape);
            CompletableFuture<Set<Release>> future = Uni.createFrom()
                    .item(() -> {
                                Set<Release> releases = fetchReleases();
                                persistScrape(scrape, releases);
                                return releases;
                            }
                    ).subscribe()
                    .asCompletionStage();

            var releases = future.get().stream()
                    .map(releaseMapper::toSearchResultArtistDto)
                    .toList();
            response = new ReleaseScrapeResponse(scrape, releases);
        }

        return response;
    }

    public Set<Release> fetchReleases() {
        Set<Release> releases = new HashSet<>();
        for (ReleaseScraper<?> scraper : releaseScrapers) {
            scrapeReleases(releases, scraper);
        }
        log.info("Total new releases found: {}", releases.size());

        return releases;
    }

    /**
     * Will persist new releases and mark artists that need to be notified
     * @param scrape
     * @param releases
     */
    @Transactional
    public void persistScrape(Scrape scrape, Collection<Release> releases) {
        scrape.setReleasesFromSpotify(
                releases.stream()
                        .filter(r -> !isNullOrEmpty(r.getSpotifyUriId()))
                        .count()
        );
        scrape.setReleasesFromMusicbrainz(
                releases.stream()
                        .filter(r -> !isNullOrEmpty(r.getMusicbrainzId()))
                        .count()
        );
        scrape.setReleaseCount(releases.size());
        scrape.setIsComplete(true);
        scrape.setCompleteDate(LocalDateTime.now());
        scrapeRepository.persist(scrape);
        updateHasNewRelease(releases);
    }

    /**
     * Mark artists that need to be notified
     * @param releases need
     */
    public void updateHasNewRelease(Collection<Release> releases) {
        List<Long> artistIds = releases.stream()
                .flatMap(release ->
                        release.getArtists()
                                .stream()
                                .map(Artist::getId)
                ).toList();
        List<UserArtist> userArtists = userArtistRepository.markNewRelease(artistIds);
        log.info("Updated {} UserArtist(s) of new releases", userArtists);
    }

    private void scrapeReleases(Set<Release> releases, ReleaseScraper<?> scraper) {
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
