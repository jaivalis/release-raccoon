package com.raccoon.artist;

import com.raccoon.dto.ArtistDto;
import com.raccoon.dto.PaginationParams;
import com.raccoon.dto.mapping.ArtistMapper;
import com.raccoon.entity.Artist;
import com.raccoon.entity.RaccoonUser;
import com.raccoon.entity.repository.ArtistRepository;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.user.dto.FollowedArtistsResponse;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import io.quarkus.panache.common.Page;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ArtistsServiceTest {

    @InjectMocks
    ArtistsService service;

    @Mock
    ArtistRepository artistRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    ArtistMapper artistMapper;

    @Captor
    ArgumentCaptor<Page> captor;

    @Test
    void getArtists_should_callRepositoryMethod() {
        var params = new PaginationParams();
        params.setPage(1);
        params.setSize(10);

        service.getArtists(params);

        verify(artistRepository).listArtistsPaginated(captor.capture());
        assertThat(captor.getValue().index).isEqualTo(10);
        assertThat(captor.getValue().size).isEqualTo(10);
    }

    @Test
    void getOtherUsersFollowedArtists_should_returnEmptyResult_when_nobodyFollowedByOthers() {
        var email = "email";
        var params = new PaginationParams();
        params.setPage(1);
        params.setSize(10);

        when(userRepository.findByEmail(any())).thenReturn(new RaccoonUser());
        when(artistRepository.listDistinctArtistsNotFollowedByUser(any(), any())).thenReturn(Collections.emptyList());

        FollowedArtistsResponse response = service.getOtherUsersFollowedArtists(params, email);

        assertThat(response.getRows())
                .isEmpty();
        assertThat(response.getTotal())
                .isZero();
    }

    @Test
    void getOtherUsersFollowedArtists_should_returnExpectedData() {
        var email = "email";
        var params = new PaginationParams();
        params.setPage(1);
        params.setSize(10);
        List<Artist> artists = List.of(new Artist());
        when(userRepository.findByEmail(any())).thenReturn(new RaccoonUser());
        when(artistRepository.listDistinctArtistsNotFollowedByUser(any(), any())).thenReturn(artists);
        ArtistDto expectedDto = new ArtistDto();
        when(artistMapper.toArtistDto(artists.get(0))).thenReturn(expectedDto);

        FollowedArtistsResponse response = service.getOtherUsersFollowedArtists(params, email);

        assertThat(response.getRows())
                .contains(expectedDto);
        assertThat(response.getTotal())
                .isEqualTo(1);
    }
}