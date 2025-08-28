package com.raccoon.templatedata.pojo;

import com.raccoon.entity.Artist;
import com.raccoon.entity.RaccoonUser;
import com.raccoon.entity.Release;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DigestMailContents {

    private RaccoonUser raccoonUser;
    private String mailTitle;
    private List<Release> releases;

    public List<ArtistReleasesGroup> getArtistReleasesGroups() {
        Map<Artist, List<Release>> groupedReleases = releases.stream()
            .filter(release -> !release.getArtists().isEmpty())
            .collect(Collectors.groupingBy(
                release -> release.getArtists().getFirst(),
                Collectors.toList()
            ));

        return groupedReleases.entrySet().stream()
            .map(entry -> ArtistReleasesGroup.builder()
                .artist(entry.getKey())
                .releases(entry.getValue())
                .build())
            .collect(Collectors.toList());
    }

}
