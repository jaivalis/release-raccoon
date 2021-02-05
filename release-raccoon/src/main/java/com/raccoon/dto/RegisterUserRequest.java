package com.raccoon.dto;

import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegisterUserRequest {

    @NotNull
    String email;

    String lastfmUsername;

}
