package com.raccoon.stats;

import com.raccoon.entity.repository.ArtistRepository;
import com.raccoon.entity.repository.ReleaseRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private ReleaseRepository releaseRepository;

    private StatsService statsService;

    @BeforeEach
    void setUp() {
        statsService = new StatsService(artistRepository, releaseRepository);
    }

    @Test
    void getReleaseCount_should_return_count_when_releases_exist() {
        when(releaseRepository.count()).thenReturn(42L);

        long result = statsService.getReleaseCount();

        assertThat(result).isEqualTo(42L);
        verify(releaseRepository).count();
    }

    @Test
    void getReleaseCount_should_return_zero_when_no_releases() {
        when(releaseRepository.count()).thenReturn(0L);

        long result = statsService.getReleaseCount();

        assertThat(result).isEqualTo(0L);
        verify(releaseRepository).count();
    }

    @Test
    void getArtistCount_should_return_count_when_artists_exist() {
        when(artistRepository.count()).thenReturn(100L);

        long result = statsService.getArtistCount();

        assertThat(result).isEqualTo(100L);
        verify(artistRepository).count();
    }

    @Test
    void getArtistCount_should_return_zero_when_no_artists() {
        when(artistRepository.count()).thenReturn(0L);

        long result = statsService.getArtistCount();

        assertThat(result).isEqualTo(0L);
        verify(artistRepository).count();
    }

    @Test
    void getAllStats_should_return_both_counts_when_data_exists() {
        when(releaseRepository.count()).thenReturn(50L);
        when(artistRepository.count()).thenReturn(25L);

        StatsService.StatsResponse result = statsService.getAllStats();

        assertThat(result).isNotNull();
        assertThat(result.releaseCount()).isEqualTo(50L);
        assertThat(result.artistCount()).isEqualTo(25L);
        verify(releaseRepository).count();
        verify(artistRepository).count();
    }

    @Test
    void getAllStats_should_return_zeros_when_no_data() {
        when(releaseRepository.count()).thenReturn(0L);
        when(artistRepository.count()).thenReturn(0L);

        StatsService.StatsResponse result = statsService.getAllStats();

        assertThat(result).isNotNull();
        assertThat(result.releaseCount()).isEqualTo(0L);
        assertThat(result.artistCount()).isEqualTo(0L);
        verify(releaseRepository).count();
        verify(artistRepository).count();
    }

    @Test
    void getAllStats_should_handle_different_counts_when_varied_data() {
        when(releaseRepository.count()).thenReturn(1000L);
        when(artistRepository.count()).thenReturn(250L);

        StatsService.StatsResponse result = statsService.getAllStats();

        assertThat(result).isNotNull();
        assertThat(result.releaseCount()).isEqualTo(1000L);
        assertThat(result.artistCount()).isEqualTo(250L);
        verify(releaseRepository).count();
        verify(artistRepository).count();
    }
}