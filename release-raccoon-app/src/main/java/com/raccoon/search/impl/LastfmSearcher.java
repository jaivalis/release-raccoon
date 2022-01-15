package com.raccoon.search.impl;

import com.raccoon.Constants;
import com.raccoon.scraper.lastfm.RaccoonLastfmApi;
import com.raccoon.search.ArtistSearcher;
import com.raccoon.search.dto.ArtistDto;
import com.raccoon.search.dto.ArtistDtoProjector;

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
    final ArtistDtoProjector artistDtoProjector;

    @Inject
    public LastfmSearcher(final RaccoonLastfmApi lastfmApi,
                          final ArtistDtoProjector artistDtoProjector) {
        this.lastfmApi = lastfmApi;
        this.artistDtoProjector = artistDtoProjector;
    }

    @Override
    public String getSearcherId() {
        return Constants.LASTFM_SEARCHER_ID;
    }

    @Override
    public Collection<ArtistDto> searchArtist(String pattern, Optional<Integer> size) {
        Stream<Artist> lastfmArtistStream = lastfmApi.searchArtist(pattern).stream();
        if (size.isPresent()) {
            lastfmArtistStream = lastfmArtistStream.limit(size.get());
        }
        return lastfmArtistStream
                .map(artistDtoProjector::project)
                .collect(Collectors.toSet());
    }

}
