package com.raccoon.templatedata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import io.quarkus.qute.Engine;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class QuteTemplateLoaderTest {

    QuteTemplateLoader loader;

    @Mock
    Engine mockEngine;

    @BeforeEach
    public void setup() throws IOException {
        MockitoAnnotations.openMocks(this);
        loader = new QuteTemplateLoader(mockEngine);
    }

    @Test
    void onStartParsesAndLoadsTemplates() {
        loader.onStart();

        verify(mockEngine, times(4)).parse(anyString());
        verify(mockEngine, times(1)).putTemplate(eq(QuteTemplateLoader.DIGEST_EMAIL_TEMPLATE_ID), any());
        verify(mockEngine, times(1)).putTemplate(eq(QuteTemplateLoader.INDEX_TEMPLATE_ID), any());
        verify(mockEngine, times(1)).putTemplate(eq(QuteTemplateLoader.PROFILE_TEMPLATE_ID), any());
        verify(mockEngine, times(1)).putTemplate(eq(QuteTemplateLoader.WELCOME_EMAIL_TEMPLATE_ID), any());
    }
}