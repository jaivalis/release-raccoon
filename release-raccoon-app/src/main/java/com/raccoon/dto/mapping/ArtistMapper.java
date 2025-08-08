package com.raccoon.dto.mapping;

import com.raccoon.dto.ArtistDto;
import com.raccoon.entity.Artist;
import com.raccoon.search.dto.SearchResultArtistDto;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI, uses = ArtistMapperResolver.class)
public interface ArtistMapper {

    SearchResultArtistDto toSearchResultArtistDto(Artist artist);

    ArtistDto toArtistDto(Artist artist);

    @Mapping(target = "id", ignore = true)
    Artist fromDto(SearchResultArtistDto dto);

    @AfterMapping
    default void addFollowerCount(@MappingTarget ArtistDto artistDto, Artist artist) {
        // Follower count will be set by the service layer to avoid lazy loading issues
    }

    @AfterMapping
    default void addFollowerCount(@MappingTarget SearchResultArtistDto artistDto, Artist artist) {
        // Follower count will be set by the service layer to avoid lazy loading issues
    }

}
