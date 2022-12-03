package com.raccoon.dto.mapping;

import com.raccoon.entity.repository.ArtistRepository;
import com.raccoon.search.dto.SearchResultArtistDto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ArtistMapperResolverTest {

    @InjectMocks
    ArtistMapperResolver resolver;

    @Mock
    ArtistRepository mockArtistRepository;

    @Test
    void resolve() {
        var dto = SearchResultArtistDto.builder()
                .name("name")
                .build();

        resolver.resolve(dto, null);

        verify(mockArtistRepository, times(1)).findByNameOptional("name");
    }

}
