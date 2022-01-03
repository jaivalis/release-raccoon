package com.raccoon.search;

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
class ArtistSearchResourceTest {

    ArtistSearchResource resource;

    @Mock
    SearchService mockSearchService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        resource = new ArtistSearchResource(mockSearchService);
    }

    @Test
    void searchArtists() {
        var pattern = "pattern";
        var limit = Optional.of(99);

        resource.searchArtists(pattern, limit);

        verify(mockSearchService, times(1)).searchArtists(pattern, limit);
    }

}
