package com.raccoon.entity.repository;

import com.raccoon.entity.User;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

    public Optional<User> findByEmailOptional(String email) {
        return Optional.ofNullable(find("email", email).firstResult());
    }

    public boolean isLastfmScrapeRequired(int scrapeIntervalDays, LocalDateTime lastLastFmScrape) {
        return lastLastFmScrape == null || !isLastLastfmScrapeLt(scrapeIntervalDays, lastLastFmScrape);
    }

    public boolean isSpotifyScrapeRequired(int scrapeIntervalDays, LocalDateTime lastSpotifyScrape) {
        return lastSpotifyScrape == null || !isLastSpotifyScrapeLt(scrapeIntervalDays, lastSpotifyScrape);
    }

////////////////////////////////////////////////////////////////////////////////////////////////////

    private boolean isLastLastfmScrapeLt(int days, LocalDateTime lastLastFmScrape) {
        return ChronoUnit.DAYS.between(lastLastFmScrape, LocalDateTime.now()) < days;
    }

    private boolean isLastSpotifyScrapeLt(int days, LocalDateTime lastSpotifyScrape) {
        return ChronoUnit.DAYS.between(lastSpotifyScrape, LocalDateTime.now()) < days;
    }

}
