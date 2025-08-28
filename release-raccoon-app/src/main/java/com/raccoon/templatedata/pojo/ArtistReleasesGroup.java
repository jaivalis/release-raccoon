package com.raccoon.templatedata.pojo;

import com.raccoon.entity.Artist;
import com.raccoon.entity.Release;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ArtistReleasesGroup {
    
    private Artist artist;
    private List<Release> releases;
    
}