package com.raccoon.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table
@AssociationOverride(name = "key.artist", joinColumns = @JoinColumn(name = "artist_id"))
@AssociationOverride(name = "key.release", joinColumns = @JoinColumn(name = "release_id"))
public class ArtistRelease extends PanacheEntityBase implements Serializable {

    @EmbeddedId
    @JsonIgnore
    ArtistReleaseKey key = new ArtistReleaseKey();

    @JsonIgnore
    public Artist getArtist() {
        return key.getArtist();
    }

    public void setArtist(Artist artist) {
        key.setArtist(artist);
    }

    @JsonIgnore
    public Release getRelease() {
        return key.getRelease();
    }

    public void setRelease(Release release) {
        key.setRelease(release);
    }

}
