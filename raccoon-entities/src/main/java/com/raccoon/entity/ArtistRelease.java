package com.raccoon.entity;

import java.io.Serializable;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.AssociationOverride;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Data;

import static com.raccoon.entity.database.Tables.ARTIST_RELEASE;

@Data
@Entity
@Table(name = ARTIST_RELEASE)
@AssociationOverride(name = "key.artist", joinColumns = @JoinColumn(name = "artist_id"))
@AssociationOverride(name = "key.release", joinColumns = @JoinColumn(name = "release_id"))
public class ArtistRelease extends PanacheEntityBase implements Serializable {

    @EmbeddedId
    @JsonbTransient
    ArtistReleaseKey key = new ArtistReleaseKey();

    @JsonbTransient
    public Artist getArtist() {
        return key.getArtist();
    }

    public void setArtist(Artist artist) {
        key.setArtist(artist);
    }

    @JsonbTransient
    public Release getRelease() {
        return key.getRelease();
    }

    public void setRelease(Release release) {
        key.setRelease(release);
    }

}
