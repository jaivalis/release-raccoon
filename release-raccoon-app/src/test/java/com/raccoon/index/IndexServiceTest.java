package com.raccoon.index;

import com.raccoon.entity.repository.ArtistRepository;
import com.raccoon.entity.repository.ReleaseRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

import static com.raccoon.templatedata.QuteTemplateLoader.INDEX_TEMPLATE_ID;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndexServiceTest {

    IndexService service;

    @Mock
    ArtistRepository artistRepositoryMock;
    @Mock
    ReleaseRepository releaseRepository;
    @Mock
    Template templateMock;

    @Mock
    TemplateInstance templateInstanceMock;
    @Mock
    Engine mockEngine;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        when(mockEngine.getTemplate(INDEX_TEMPLATE_ID)).thenReturn(templateMock);

        service = new IndexService(artistRepositoryMock, releaseRepository, mockEngine);
    }

    @Test
    void getIndex() {
        when(artistRepositoryMock.count()).thenReturn(2222L);
        when(releaseRepository.count()).thenReturn(3333L);
        when(templateMock.data(anyString(), eq("2222"), anyString(), eq("3333"))).thenReturn(templateInstanceMock);

        service.getTemplateInstance();

        verify(templateMock, times(1)).data(anyString(), anyString(), anyString(), anyString());
    }
}