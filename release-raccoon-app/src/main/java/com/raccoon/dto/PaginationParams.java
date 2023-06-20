package com.raccoon.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Positive;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;

import lombok.Data;

@Data
public class PaginationParams {

    @QueryParam("page")
    @DefaultValue("0")
    @Positive
    private int page;

    @QueryParam("size")
    @DefaultValue("10")
    @Positive
    @Max(100)
    private int size;

}