package com.raccoon.templatedata.pojo;

import com.raccoon.entity.UserArtist;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ProfileContents {

    private List<UserArtist> artistsFollowed;
    private boolean spotifyEnabled;
    private boolean lastfmEnabled;
    private boolean canScrapeSpotify;
    private boolean canScrapeLastfm;

}
