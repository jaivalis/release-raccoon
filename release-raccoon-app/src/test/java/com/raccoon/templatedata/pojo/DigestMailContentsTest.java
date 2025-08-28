package com.raccoon.templatedata.pojo;

import com.raccoon.entity.Artist;
import com.raccoon.entity.ArtistRelease;
import com.raccoon.entity.Release;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DigestMailContentsTest {

    @Test
    void getArtistReleasesGroups_should_group_releases_by_artist_when_multiple_releases_per_artist() {
        var artist1 = createArtist(1L, "Artist One");
        var artist2 = createArtist(2L, "Artist Two");
        
        var release1 = createReleaseWithArtist(1L, "Album 1", "album", artist1);
        var release2 = createReleaseWithArtist(2L, "Single 1", "single", artist1);
        var release3 = createReleaseWithArtist(3L, "Album 2", "album", artist2);
        
        var releases = List.of(release1, release2, release3);
        var contents = DigestMailContents.builder()
                .releases(releases)
                .build();

        var groups = contents.getArtistReleasesGroups();

        assertThat(groups).hasSize(2);
        
        var artist1Group = groups.stream()
                .filter(group -> group.getArtist().equals(artist1))
                .findFirst()
                .orElseThrow();
        assertThat(artist1Group.getReleases()).hasSize(2);
        assertThat(artist1Group.getReleases()).containsExactlyInAnyOrder(release1, release2);
        
        var artist2Group = groups.stream()
                .filter(group -> group.getArtist().equals(artist2))
                .findFirst()
                .orElseThrow();
        assertThat(artist2Group.getReleases()).hasSize(1);
        assertThat(artist2Group.getReleases()).contains(release3);
    }

    @Test
    void getArtistReleasesGroups_should_handle_single_release_per_artist() {
        var artist1 = createArtist(1L, "Artist One");
        var artist2 = createArtist(2L, "Artist Two");
        
        var release1 = createReleaseWithArtist(1L, "Album 1", "album", artist1);
        var release2 = createReleaseWithArtist(2L, "Single 1", "single", artist2);
        
        var releases = List.of(release1, release2);
        var contents = DigestMailContents.builder()
                .releases(releases)
                .build();

        var groups = contents.getArtistReleasesGroups();

        assertThat(groups).hasSize(2);
        assertThat(groups).allMatch(group -> group.getReleases().size() == 1);
    }

    @Test
    void getArtistReleasesGroups_should_return_empty_list_when_no_releases() {
        var contents = DigestMailContents.builder()
                .releases(List.of())
                .build();

        var groups = contents.getArtistReleasesGroups();

        assertThat(groups).isEmpty();
    }

    @Test
    void getArtistReleasesGroups_should_filter_out_releases_with_no_artists() {
        var artist1 = createArtist(1L, "Artist One");
        var release1 = createReleaseWithArtist(1L, "Album 1", "album", artist1);
        var release2 = createReleaseWithoutArtist(2L, "Orphan Release", "single");
        
        var releases = List.of(release1, release2);
        var contents = DigestMailContents.builder()
                .releases(releases)
                .build();

        var groups = contents.getArtistReleasesGroups();

        assertThat(groups).hasSize(1);
        assertThat(groups.get(0).getArtist()).isEqualTo(artist1);
        assertThat(groups.get(0).getReleases()).containsOnly(release1);
    }

    private Artist createArtist(Long id, String name) {
        var artist = new Artist();
        artist.id = id;
        artist.setName(name);
        return artist;
    }

    private Release createReleaseWithArtist(Long id, String name, String type, Artist artist) {
        var release = new Release();
        release.id = id;
        release.setName(name);
        release.setType(type);
        release.setReleasedOn(LocalDate.now());
        
        var artistRelease = new ArtistRelease();
        artistRelease.setArtist(artist);
        release.getReleases().add(artistRelease);
        
        return release;
    }

    private Release createReleaseWithoutArtist(Long id, String name, String type) {
        var release = new Release();
        release.id = id;
        release.setName(name);
        release.setType(type);
        release.setReleasedOn(LocalDate.now());
        return release;
    }
}