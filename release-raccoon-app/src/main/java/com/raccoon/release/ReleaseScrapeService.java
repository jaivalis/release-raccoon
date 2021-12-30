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
import javax.transaction.Transactional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class ReleaseScrapeService {

    ReleaseScrapers releaseScrapers;
    UserArtistRepository userArtistRepository;

    @Inject
    ReleaseScrapeService(final ReleaseScrapers releaseScrapers,
                         final UserArtistRepository userArtistRepository) {
        this.releaseScrapers = releaseScrapers;
        this.userArtistRepository = userArtistRepository;
    }

    @Transactional
    public Set<Release> scrape() throws ReleaseScrapeException, InterruptedException {
        try {
            // Could optimize the txs by localizing and batching.
            Set<Release> releases = releaseScrapers.scrape();
            updateHasNewRelease(releases);

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
