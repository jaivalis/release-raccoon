package com.raccoon.search.impl;

import com.raccoon.Constants;
import com.raccoon.scraper.lastfm.RaccoonLastfmApi;
import com.raccoon.search.ArtistSearcher;
import com.raccoon.search.dto.ArtistDto;
import com.raccoon.search.dto.mapping.LastFmArtistMapper;

import de.umass.lastfm.Artist;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class LastfmSearcher implements ArtistSearcher {

    final RaccoonLastfmApi lastfmApi;
    final LastFmArtistMapper artistMapper;

    @Inject
    public LastfmSearcher(final RaccoonLastfmApi lastfmApi,
                          final LastFmArtistMapper artistMapper) {
        this.lastfmApi = lastfmApi;
        this.artistMapper = artistMapper;
    }

    @Override
    public String id() {
        return Constants.LASTFM_SEARCHER_ID;
    }

    @Override
    public Double trustworthiness() {
        return .5;
    }

    @Override
    public Collection<ArtistDto> searchArtist(String pattern, Optional<Integer> size) {
        Stream<Artist> lastfmArtistStream = lastfmApi.searchArtist(pattern).stream();
        if (size.isPresent()) {
            lastfmArtistStream = lastfmArtistStream.limit(size.get());
        }
        return lastfmArtistStream
                .map(artistMapper::toDto)
                .collect(Collectors.toSet());
    }

}
