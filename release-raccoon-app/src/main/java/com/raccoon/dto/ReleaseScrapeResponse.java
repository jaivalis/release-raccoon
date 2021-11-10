package com.raccoon.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public record ReleaseScrapeResponse(@JsonProperty @NotNull Boolean success,
                                    @JsonProperty String message) {
}
