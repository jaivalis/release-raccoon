package com.raccoon.search.impl;

import com.raccoon.Constants;
import com.raccoon.dto.mapping.ArtistMapper;
import com.raccoon.entity.Artist;
import com.raccoon.search.ArtistSearcher;
import com.raccoon.search.dto.SearchResultArtistDto;

import org.hibernate.search.mapper.orm.session.SearchSession;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class HibernateSearcher implements ArtistSearcher {

    final SearchSession searchSession;
    final ArtistMapper artistMapper;

    @Inject
    public HibernateSearcher(final SearchSession searchSession,
                             final ArtistMapper artistMapper) {
        this.searchSession = searchSession;
        this.artistMapper = artistMapper;
    }

    @Override
    public String id() {
        return Constants.HIBERNATE_SEARCHER_ID;
    }

    @Override
    public Double trustworthiness() {
        return Constants.HIBERNATE_SEARCHER_TRUSTWORTHINESS;
    }

    @Override
    @Transactional
    public Collection<SearchResultArtistDto> searchArtist(String pattern, Optional<Integer> size) {
        return searchSession.search(Artist.class)
                .where(f ->
                        pattern == null || pattern.trim().isEmpty() ?
                                f.matchAll() :
                                f.simpleQueryString()
                                        .fields("name").matching(pattern)
                )
                .fetchHits(size.orElse(20))
                .stream()
                .map(artistMapper::toSearchResultArtistDto)
                .collect(Collectors.toSet());
    }

}
