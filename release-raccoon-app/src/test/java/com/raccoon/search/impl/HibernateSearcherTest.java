package com.raccoon.search.impl;

import com.raccoon.entity.Artist;
import com.raccoon.search.dto.ArtistDtoProjector;

import org.hibernate.search.mapper.orm.massindexing.MassIndexer;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HibernateSearcherTest {

    HibernateSearcher searcher;

    @Mock
    SearchSession mockSearchSession;
    @Mock
    ArtistDtoProjector mockArtistDtoProjector;


    @BeforeEach
    void setUp() {
        searcher = new HibernateSearcher(mockSearchSession, mockArtistDtoProjector);
    }

    @Test
    void onStart() {
        var mockIndexer = mock(MassIndexer.class);
        when(mockIndexer.threadsToLoadObjects(anyInt())).thenReturn(mockIndexer);
        when(mockIndexer.batchSizeToLoadObjects(anyInt())).thenReturn(mockIndexer);
        when(mockSearchSession.massIndexer(Artist.class)).thenReturn(mockIndexer);

        searcher.onStart(null);

        verify(mockSearchSession, times(1)).massIndexer(Artist.class);
        verify(mockIndexer, times(1)).start();
    }

}
