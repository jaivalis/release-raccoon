package com.raccoon.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class RegisterUserRequest {

    @NotNull
    String email;

    String lastfmUsername;
}
