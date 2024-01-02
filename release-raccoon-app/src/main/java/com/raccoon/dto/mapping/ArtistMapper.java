package com.raccoon.dto.mapping;

import com.raccoon.dto.ArtistDto;
import com.raccoon.entity.Artist;
import com.raccoon.search.dto.SearchResultArtistDto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA, uses = ArtistMapperResolver.class)
public interface ArtistMapper {

    SearchResultArtistDto toSearchResultArtistDto(Artist artist);

    ArtistDto toArtistDto(Artist artist);

    @Mapping(target = "id", ignore = true)
    Artist fromDto(SearchResultArtistDto dto);

}
