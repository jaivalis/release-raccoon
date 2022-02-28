package com.raccoon.search.impl;

import com.raccoon.Constants;
import com.raccoon.scraper.musicbrainz.MusicbrainzClient;
import com.raccoon.scraper.musicbrainz.dto.MusicbrainzArtist;
import com.raccoon.scraper.musicbrainz.dto.MusicbrainzArtistsResponse;
import com.raccoon.search.ArtistSearcher;
import com.raccoon.search.dto.ArtistDto;
import com.raccoon.search.dto.mapping.MusicbrainzArtistMapper;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

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
        return .8;
    }

    @Override
    public Collection<ArtistDto> searchArtist(String pattern, Optional<Integer> size) {
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
    }

}
