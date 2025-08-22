package com.raccoon.stats;

import com.raccoon.entity.repository.ArtistRepository;
import com.raccoon.entity.repository.ReleaseRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class StatsService {

    private final ArtistRepository artistRepository;
    private final ReleaseRepository releaseRepository;

    @Inject
    public StatsService(final ArtistRepository artistRepository,
                       final ReleaseRepository releaseRepository) {
        this.artistRepository = artistRepository;
        this.releaseRepository = releaseRepository;
    }

    public long getReleaseCount() {
        long count = releaseRepository.count();
        log.debug("Release count: {}", count);
        return count;
    }

    public long getArtistCount() {
        long count = artistRepository.count();
        log.debug("Artist count: {}", count);
        return count;
    }

    public StatsResponse getAllStats() {
        long releaseCount = getReleaseCount();
        long artistCount = getArtistCount();
        log.debug("Stats - Releases: {}, Artists: {}", releaseCount, artistCount);
        return new StatsResponse(releaseCount, artistCount);
    }

    public record StatsResponse(long releaseCount, long artistCount) {}
}