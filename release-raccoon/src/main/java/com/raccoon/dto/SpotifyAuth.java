package com.raccoon.dto;

import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class SpotifyAuth {

    @NotNull
    String code;

    String state;

}
