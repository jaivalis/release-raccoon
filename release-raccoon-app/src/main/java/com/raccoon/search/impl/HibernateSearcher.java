package com.raccoon.search.impl;

import com.raccoon.entity.Artist;
import com.raccoon.search.ArtistSearcher;
import com.raccoon.search.dto.ArtistDto;
import com.raccoon.search.dto.ArtistDtoProjector;

import org.hibernate.search.mapper.orm.session.SearchSession;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;

import io.quarkus.runtime.StartupEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class HibernateSearcher implements ArtistSearcher {

    final SearchSession searchSession;
    final ArtistDtoProjector artistDtoProjector;

    @Inject
    public HibernateSearcher(final SearchSession searchSession,
                             final ArtistDtoProjector artistDtoProjector) {
        this.searchSession = searchSession;
        this.artistDtoProjector = artistDtoProjector;
    }

    @Transactional
    void onStart(@Observes StartupEvent ev) {
        log.info("Indexing");
        searchSession.massIndexer(Artist.class)
                .threadsToLoadObjects(2)
                .batchSizeToLoadObjects(25)
                .start();
    }

    @Override
    @Transactional
    public Collection<ArtistDto> searchArtist(String pattern, Optional<Integer> size) {
        return searchSession.search(Artist.class)
                .where(f ->
                        pattern == null || pattern.trim().isEmpty() ?
                                f.matchAll() :
                                f.simpleQueryString()
                                        .fields("name").matching(pattern)
                )
                .fetchHits(size.orElse(20))
                .stream()
                .map(artistDtoProjector::project)
                .collect(Collectors.toSet());
    }

}
