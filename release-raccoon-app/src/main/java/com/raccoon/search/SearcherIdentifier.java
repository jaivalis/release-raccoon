package com.raccoon.search;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearcherIdentifier {

    private String id;
    private Double trustworthiness;

}
