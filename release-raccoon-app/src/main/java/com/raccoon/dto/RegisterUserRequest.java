package com.raccoon.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public record RegisterUserRequest(
        @JsonProperty @NotNull String email,
        @JsonProperty String lastfmUsername,
        @JsonProperty Boolean spotifyEnabled) {}
