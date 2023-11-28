package com.raccoon.search.impl;

import com.raccoon.Constants;
import com.raccoon.scraper.musicbrainz.MusicbrainzClient;
import com.raccoon.scraper.musicbrainz.dto.MusicbrainzArtist;
import com.raccoon.scraper.musicbrainz.dto.MusicbrainzArtistsResponse;
import com.raccoon.search.ArtistSearcher;
import com.raccoon.search.dto.SearchResultArtistDto;
import com.raccoon.search.dto.mapping.MusicbrainzArtistMapper;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class MusicbrainzSearcher implements ArtistSearcher {

    final MusicbrainzClient musicbrainzClient;
    final MusicbrainzArtistMapper artistMapper;

    @Inject
    public MusicbrainzSearcher(final MusicbrainzClient musicbrainzClient,
                               final MusicbrainzArtistMapper artistMapper) {
        this.musicbrainzClient = musicbrainzClient;
        this.artistMapper = artistMapper;
    }

    @Override
    public String id() {
        return Constants.MUSICBRAINZ_SEARCHER_ID;
    }

    @Override
    public Double trustworthiness() {
        return Constants.MUSICBRAINZ_SEARCHER_TRUSTWORTHINESS;
    }

    @Override
    public Collection<SearchResultArtistDto> searchArtist(String pattern, Optional<Integer> size) {
        try {
            MusicbrainzArtistsResponse response = musicbrainzClient.searchArtistsByName(pattern, size.orElse(20), 0);
            if (response == null || response.getCount() == 0 || response.getArtists() == null) {
                return Collections.emptyList();
            }

            Stream<MusicbrainzArtist> artists = response.getArtists().stream();
            if (size.isPresent()) {
                artists = artists.limit(size.get());
            }

            return artists
                    .map(artistMapper::toDto)
                    .collect(Collectors.toSet());
        } catch (RuntimeException e) {
            log.error("Something went wrong when trying to scrape Musicbrainz", e);
            return Collections.emptyList();
        }
    }

}
