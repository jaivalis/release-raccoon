package com.raccoon.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import lombok.Data;

@Data
public class PaginationParams {

    @QueryParam("page")
    @DefaultValue("0")
    @PositiveOrZero
    private int page;

    @QueryParam("size")
    @DefaultValue("10")
    @Positive
    @Max(100)
    private int size;

}