package com.raccoon.entity;

import com.raccoon.common.ExcludeFromJacocoGeneratedReport;

import org.hibernate.search.backend.elasticsearch.analysis.ElasticsearchAnalysisConfigurationContext;
import org.hibernate.search.backend.elasticsearch.analysis.ElasticsearchAnalysisConfigurer;

import io.quarkus.hibernate.search.orm.elasticsearch.SearchExtension;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Named;

@Dependent
@Named("raccoonAnalysisConfigurer")
@SearchExtension
@ExcludeFromJacocoGeneratedReport
public class AnalysisConfigurer implements ElasticsearchAnalysisConfigurer {

    @Override
    public void configure(ElasticsearchAnalysisConfigurationContext context) {
        context.analyzer("name")
                .custom()
                .tokenizer("standard")
                .tokenFilters("asciifolding", "lowercase");
    }

}
