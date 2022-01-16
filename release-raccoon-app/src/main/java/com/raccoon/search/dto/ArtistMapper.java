package com.raccoon.search.dto;

import com.raccoon.entity.Artist;

import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi", uses = ArtistMapperResolver.class)
public interface ArtistMapper {

    ArtistDto toDto(Artist person);

    Artist fromDto(ArtistDto dto);

}
