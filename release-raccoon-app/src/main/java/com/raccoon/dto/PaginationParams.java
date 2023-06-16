package com.raccoon.dto;

import javax.validation.constraints.Max;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;

import lombok.Data;

@Data
public class PaginationParams {

    @QueryParam("page")
    @DefaultValue("0")
    private int page;

    @QueryParam("size")
    @DefaultValue("10")
    @Max(100)
    private int size;

}