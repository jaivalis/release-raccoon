package com.raccoon.scraper.musicbrainz;

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

    @Inject
    public MusicbrainzClient(@RestClient MusicbrainzService musicbrainzService) {
        this.musicbrainzService = musicbrainzService;
    }

    /**
     * Queries MusicbrainzService for Releases occurred after a specific date.
     * @param date the date for which to query Musicbrainz API
     * @param offset the date for which to query Musicbrainz API
     * @return Optional<MusicbrainzReleasesResponse> response wrapped
     */
    public MusicbrainzReleasesResponse getForDate(LocalDate date, int offset) {
        var query = formatQuery(date);
        log.info("Executing Musicbrainz query {}, offset {}", query, offset);
        return musicbrainzService.getReleasesByQuery(query, "json", "100", String.valueOf(offset));
    }

    String formatQuery(LocalDate date) {
        var formatted = DATE_TIME_FORMATTER.format(date);
        return "date:(" + formatted + ")";
    }

}
