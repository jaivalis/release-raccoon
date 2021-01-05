package com.raccoon.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Data;

import javax.persistence.*;

//@Table(name="User_Artist")

@Data
//@Entity
//@Table(name = "ArtistRelease")
@Entity
@Table
//@AssociationOverride(name = "key.artist", joinColumns = @JoinColumn(name = "artist_id"))
//@AssociationOverrides({
        @AssociationOverride(name = "key.artist", joinColumns = @JoinColumn(name = "artist_id"))
        @AssociationOverride(name = "key.notRelease", joinColumns = @JoinColumn(name = "release_id"))
//})
public class ArtistRelease extends PanacheEntityBase {

    @EmbeddedId
    ArtistReleaseKey key;

    public Artist getArtist() {
        return key.getArtist();
    }

    public void setArtist(Artist artist) {
        key.setArtist(artist);
    }

    public NotRelease getNotRelease() {
        return key.getNotRelease();
    }

    public void getNotRelease(NotRelease notRelease) {
        key.setNotRelease(notRelease);
    }

//    @ManyToOne
//    @MapsId("artistId")
//    @JoinColumn//(name = "artist_id")
//    private Artist artist;

//    @ManyToOne
//    @MapsId("releaseId")
//    @JoinColumn(name = "release_id")
//    private Release release;

//    @Id
//    Long artistId;
//
//    @Id
//    Long releaseId;

}
