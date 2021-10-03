package com.raccoon.index;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class IndexResourceTest {

    IndexResource resource;

    @Mock
    IndexService mockService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        resource = new IndexResource(mockService);
    }

    @Test
    void scrapeReleases() {
        resource.index();

        verify(mockService, times(1)).getTemplateInstance();
    }
}