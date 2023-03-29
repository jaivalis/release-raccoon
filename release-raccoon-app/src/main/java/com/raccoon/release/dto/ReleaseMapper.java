package com.raccoon.release.dto;

import com.raccoon.entity.Release;

import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi")
public interface ReleaseMapper {

    ReleaseDto toSearchResultArtistDto(Release release);

}
