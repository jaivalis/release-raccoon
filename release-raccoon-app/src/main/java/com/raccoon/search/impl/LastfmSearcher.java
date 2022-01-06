package com.raccoon.search.impl;

import com.raccoon.scraper.lastfm.RaccoonLastfmApi;
import com.raccoon.search.ArtistSearcher;
import com.raccoon.search.dto.ArtistDto;
import com.raccoon.search.dto.ArtistDtoProjector;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class LastfmSearcher implements ArtistSearcher {

    final RaccoonLastfmApi lastfmApi;
    final ArtistDtoProjector artistDtoProjector;

    @Inject
    public LastfmSearcher(final RaccoonLastfmApi lastfmApi,
                          final ArtistDtoProjector artistDtoProjector) {
        this.lastfmApi = lastfmApi;
        this.artistDtoProjector = artistDtoProjector;
    }

    @Override
    public Collection<ArtistDto> searchArtist(String pattern, Optional<Integer> size) {
        return lastfmApi.searchArtist(pattern)
                .stream()
                .map(artistDtoProjector::project)
                .collect(Collectors.toSet());
    }

}
