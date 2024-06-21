package com.raccoon.entity.factory;

import com.raccoon.entity.Artist;
import com.raccoon.entity.repository.ArtistRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import io.quarkus.test.TestTransaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestTransaction
class ArtistFactoryTest {

    ArtistFactory factory;

    @Mock
    ArtistRepository artistRepository;

    @BeforeEach
    void setUp() {
        factory = new ArtistFactory(artistRepository);
    }

    @Test
    void getOrCreateArtistExists() {
        var artistName = "artist";
        var artist = new Artist();
        when(artistRepository.findByNameOptional(any()))
                .thenReturn(Optional.of(artist));

        factory.getOrCreateArtist(artistName);

        verify(artistRepository, times(0)).persist(any(Artist.class));
    }

    @Test
    void getOrCreateArtistCreate() {
        var artistName = "artist";
        when(artistRepository.findByNameOptional(any()))
                .thenReturn(Optional.empty());

        var created = factory.getOrCreateArtist(artistName);

        assertEquals(artistName, created.getName());
        verify(artistRepository, times(1)).persist(any(Artist.class));
    }
}