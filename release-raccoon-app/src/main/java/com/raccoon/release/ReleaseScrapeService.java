package com.raccoon.release;

import com.raccoon.entity.Artist;
import com.raccoon.entity.Release;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.scraper.ReleaseScraper;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.transaction.Transactional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class ReleaseScrapeService {

    List<ReleaseScraper<?>> releaseScrapers;
    UserArtistRepository userArtistRepository;

    @Inject
    ReleaseScrapeService(final Instance<ReleaseScraper<?>> releaseScrapers,
                         final UserArtistRepository userArtistRepository) {
        this.releaseScrapers = releaseScrapers.stream().toList();
        log.info("Found {} release scrapers in classpath", this.releaseScrapers.size());
        this.userArtistRepository = userArtistRepository;
    }

    public Set<Release> scrapeReleases() throws InterruptedException {
        Set<Release> releases = new HashSet<>();
        for (ReleaseScraper<?> scraper : releaseScrapers) {
            Set<Release> releasesPerScraper = scraper.scrapeReleases(Optional.empty());
            releases.addAll(releasesPerScraper);
            log.info("New releases found through {}: {}", scraper.getClass().getSimpleName(), releasesPerScraper.size());
        }
        log.info("Total new releases found: {}", releases.size());

        return releases;
    }

    @Transactional
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

}
