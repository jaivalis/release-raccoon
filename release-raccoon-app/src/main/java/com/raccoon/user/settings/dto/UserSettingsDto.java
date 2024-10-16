package com.raccoon.user.settings.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(NON_NULL)
@Data
public class UserSettingsDto {

    private Integer notifyIntervalDays;
    private Boolean unsubscribed;

}
