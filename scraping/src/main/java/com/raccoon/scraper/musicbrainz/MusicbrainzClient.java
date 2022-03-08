package com.raccoon.scraper.musicbrainz;

import com.google.common.util.concurrent.RateLimiter;

import com.raccoon.scraper.musicbrainz.dto.MusicbrainzArtistsResponse;
import com.raccoon.scraper.musicbrainz.dto.MusicbrainzReleasesResponse;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class MusicbrainzClient {

    @RestClient
    MusicbrainzService musicbrainzService;

    private static final String ENCODED_DATE_PATTERN = "yyyy\\-MM\\-dd";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(ENCODED_DATE_PATTERN);
    private final RateLimiter rateLimiter = RateLimiter.create(2);

    @Inject
    public MusicbrainzClient(@RestClient MusicbrainzService musicbrainzService) {
        this.musicbrainzService = musicbrainzService;
    }

    /**
     * Queries MusicbrainzService for Releases released on a specific date.
     * @param date the date for which to query Musicbrainz API
     * @param offset the date for which to query Musicbrainz API
     * @return Optional<MusicbrainzReleasesResponse> response wrapped
     */
    public MusicbrainzReleasesResponse searchReleasesByDate(LocalDate date, int offset) {
        var query = formatDateQuery(date);
        rateLimiter.tryAcquire();
        log.info("Executing Musicbrainz release query {}, offset {}", query, offset);
        return musicbrainzService.getReleasesByQuery(query, "json", "100", String.valueOf(offset));
    }

    /**
     * Queries MusicbrainzService for Artists with a given name.
     * @param name the name for which to query Musicbrainz API
     * @param offset the date for which to query Musicbrainz API
     * @return Optional<MusicbrainzReleasesResponse> response wrapped
     */
    public MusicbrainzArtistsResponse searchArtistsByName(String name, int limit, int offset) {
        var query = formatNameQuery(name);
        rateLimiter.tryAcquire();
        log.info("Executing Musicbrainz artist query {}, offset {}", name, offset);
        return musicbrainzService.getArtistsByQuery(query, "json", String.valueOf(limit), String.valueOf(offset));
    }

    String formatDateQuery(LocalDate date) {
        var formatted = DATE_TIME_FORMATTER.format(date);
        return "date:(" + formatted + ")";
    }

    String formatNameQuery(String name) {
        return name.replace(" ", "+");
    }

}
