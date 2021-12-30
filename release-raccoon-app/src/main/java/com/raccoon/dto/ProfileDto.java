package com.raccoon.dto;

import com.raccoon.entity.Artist;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ProfileDto {

    private List<Artist> artistsFollowed;
    private boolean spotifyEnabled;
    private boolean lastfmEnabled;
    private boolean canScrapeSpotify;
    private boolean canScrapeLastfm;

}
