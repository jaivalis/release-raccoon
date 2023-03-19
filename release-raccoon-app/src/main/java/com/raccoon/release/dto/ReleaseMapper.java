package com.raccoon.release.dto;

import com.raccoon.entity.Release;

import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi")
public interface ReleaseMapper {

    ReleaseDto toSearchResultArtistDto(Release release);

//    @Mapper(componentModel = "cdi", uses = ArtistMapperResolver.class)
//public interface ArtistMapper {
//
//    SearchResultArtistDto toSearchResultArtistDto(Artist artist);
//
//    FollowedArtistDto toFollowedArtistDto(Artist artist);
//
//    @Mapping(target = "id", ignore = true)
//    Artist fromDto(SearchResultArtistDto dto);
//
//}
}
