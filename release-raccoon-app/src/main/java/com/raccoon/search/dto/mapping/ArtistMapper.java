package com.raccoon.search.dto.mapping;

import com.raccoon.entity.Artist;
import com.raccoon.search.dto.ArtistDto;

import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi", uses = ArtistMapperResolver.class)
public interface ArtistMapper {

    ArtistDto toDto(Artist artist);

    Artist fromDto(ArtistDto dto);

}
