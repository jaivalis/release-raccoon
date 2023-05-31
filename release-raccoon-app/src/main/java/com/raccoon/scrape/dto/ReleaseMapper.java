package com.raccoon.scrape.dto;

import com.raccoon.entity.Release;

import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi")
public interface ReleaseMapper {

    ReleaseDto toSearchResultArtistDto(Release release);

}
