package com.raccoon.search.dto.mapping;

import com.raccoon.entity.repository.ArtistRepository;
import com.raccoon.search.dto.ArtistDto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;

@ExtendWith(MockitoExtension.class)
class ArtistMapperResolverTest {

    ArtistMapperResolver resolver;

    @Mock
    ArtistRepository mockArtistRepository;

    @BeforeEach
    void setUp() {
        openMocks(this);
        resolver = new ArtistMapperResolver(mockArtistRepository);
    }

    @Test
    void resolve() {
        var dto = ArtistDto.builder()
                .name("name")
                .build();

        resolver.resolve(dto, null);

        verify(mockArtistRepository, times(1)).findByNameOptional("name");
    }

}