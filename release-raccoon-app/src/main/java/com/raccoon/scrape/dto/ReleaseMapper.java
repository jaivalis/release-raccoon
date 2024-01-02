package com.raccoon.scrape.dto;

import com.raccoon.entity.Release;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface ReleaseMapper {

    ReleaseDto toSearchResultArtistDto(Release release);

}
