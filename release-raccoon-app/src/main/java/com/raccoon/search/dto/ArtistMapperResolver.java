package com.raccoon.search.dto;

import com.raccoon.entity.Artist;
import com.raccoon.entity.repository.ArtistRepository;

import org.mapstruct.ObjectFactory;
import org.mapstruct.TargetType;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class ArtistMapperResolver {

    private final ArtistRepository artistRepository;

    @Inject
    ArtistMapperResolver(final ArtistRepository userRepository) {
        this.artistRepository = userRepository;
    }

    @ObjectFactory
    public Artist resolve(ArtistDto dto, @TargetType Class<Artist> type) {
        return artistRepository.findByNameOptional(dto.getName())
                .orElseGet(Artist::new);
    }

}
