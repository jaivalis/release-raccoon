package com.raccoon.search;

import com.raccoon.search.impl.HibernateSearcher;
import com.raccoon.search.impl.LastfmSearcher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    SearchService service;
    
    @Mock
    HibernateSearcher mockHibernateSearcher;
    @Mock
    LastfmSearcher mockLastfmSearcher;

    @BeforeEach
    void setUp() {
        openMocks(this);
        service = new SearchService(mockHibernateSearcher, mockLastfmSearcher);
    }

    @Test
    void searchArtists() {
        var pattern = "pattern";
        var size = Optional.of(10);

        service.searchArtists(pattern, Optional.of(10));

        verify(mockHibernateSearcher, times(1)).searchArtist(pattern, size);
        verify(mockLastfmSearcher, times(1)).searchArtist(pattern, size);
    }

}
