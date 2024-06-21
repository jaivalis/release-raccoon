package com.raccoon.entity.repository;

import com.raccoon.entity.Scrape;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.IntStream;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
@TestTransaction
class ScrapeRepositoryTest {

    @Inject
    ScrapeRepository repository;

    @Test
    void getMostRecentScrapeFrom_should_beEmpty_when_allScrapesAreOld() {
        IntStream.range(0, 10)
                .mapToObj(i -> {
                    var scrape = new Scrape();
                    scrape.setCompleteDate(LocalDateTime.now().minusDays(7));
                    return scrape;
                })
                .forEach(repository::persist);

        Optional<Scrape> mostRecent = repository.getMostRecentScrapeFrom(LocalDateTime.now().minusDays(1));

        assertThat(mostRecent)
                .isEmpty();
    }

    @Test
    void getMostRecentScrapeFrom_should_returnMostRecent_when_allScrapesAreRecent() {
        var scrape1 = new Scrape();
        scrape1.setCompleteDate(LocalDateTime.now().minusMinutes(4));
        scrape1.setIsComplete(true);
        scrape1.setReleasesFromMusicbrainz(88L);
        var scrape2 = new Scrape();
        scrape2.setCompleteDate(LocalDateTime.now().minusMinutes(3));
        scrape2.setIsComplete(true);
        scrape2.setReleasesFromMusicbrainz(99L);
        var scrape3 = new Scrape();
        LocalDateTime mostRecentTimestamp = LocalDateTime.now().minusMinutes(2);
        scrape3.setCompleteDate(mostRecentTimestamp);
        scrape3.setIsComplete(true);
        scrape3.setReleasesFromMusicbrainz(199L);
        repository.persist(scrape1, scrape2, scrape3);

        var oneDayAgo = LocalDateTime.now().minusDays(1);
        var mostRecentSinceYesterday = repository.getMostRecentScrapeFrom(oneDayAgo);

        assertThat(mostRecentSinceYesterday)
                .isPresent();
        assertThat(mostRecentSinceYesterday.get().getReleasesFromMusicbrainz())
                .isEqualTo(scrape3.getReleasesFromMusicbrainz());
    }

    @Test
    void getMostRecentScrapeFrom_should_returnEmpty_when_completeDateNotSet() {
        IntStream.range(0, 10)
                .mapToObj(i -> {
                    var scrape = new Scrape();
                    scrape.setCompleteDate(null);
                    return scrape;
                })
                .forEach(repository::persist);

        var oneDayAgo = LocalDateTime.now().minusDays(1);
        var mostRecent = repository.getMostRecentScrapeFrom(oneDayAgo);

        assertThat(mostRecent)
                .isEmpty();
    }

}