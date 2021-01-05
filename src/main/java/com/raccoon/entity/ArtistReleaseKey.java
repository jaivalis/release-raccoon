package com.raccoon.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@Data
@Embeddable
@NoArgsConstructor
public class ArtistReleaseKey implements Serializable {

//    @Column(name = "artist_id")
//    Long artistId;
//
//    @Column(name = "release_id")
//    Long releaseId;

    @ManyToOne(fetch = FetchType.LAZY)
    private Artist artist;

    @ManyToOne
    private NotRelease notRelease;
}
