package com.raccoon.entity.repository;

import com.raccoon.entity.Scrape;

import java.time.LocalDateTime;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;

@ApplicationScoped
public class ScrapeRepository implements PanacheRepository<Scrape> {

    /**
     * Retrieves the most recent {@code Scrape} which needs to be complete since at least gte.
     *
     * @param gte the minimum date on which the {@code Scrape} was complete
     * @return
     */
    public Optional<Scrape> getMostRecentScrapeFrom(LocalDateTime gte) {
        return find("completeDate > ?1", Sort.by("completeDate", Sort.Direction.Descending), gte)
                .stream()
                .findFirst();
    }

}
