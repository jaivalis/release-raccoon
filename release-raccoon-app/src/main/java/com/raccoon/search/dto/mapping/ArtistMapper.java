package com.raccoon.search.dto.mapping;

import com.raccoon.entity.Artist;
import com.raccoon.search.dto.SearchResultArtistDto;
import com.raccoon.user.dto.FollowedArtistDto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// TODO this needs to be moved out
@Mapper(componentModel = "cdi", uses = ArtistMapperResolver.class)
public interface ArtistMapper {

    SearchResultArtistDto toDto(Artist artist);

    FollowedArtistDto toFollowedArtistDto(Artist artist);

    @Mapping(target = "id", ignore = true)
    Artist fromDto(SearchResultArtistDto dto);

}
