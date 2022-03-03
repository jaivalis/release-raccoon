package com.raccoon.search.dto.mapping;

import com.raccoon.entity.Artist;
import com.raccoon.search.dto.ArtistDto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi", uses = ArtistMapperResolver.class)
public interface ArtistMapper {

    ArtistDto toDto(Artist artist);

    @Mapping(target = "id", ignore = true)
    Artist fromDto(ArtistDto dto);

}
