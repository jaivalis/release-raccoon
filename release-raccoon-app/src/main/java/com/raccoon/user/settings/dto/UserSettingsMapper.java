package com.raccoon.user.settings.dto;

import com.raccoon.entity.UserSettings;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface UserSettingsMapper {

    UserSettingsDto toUserSettingsDto(UserSettings release);

}
