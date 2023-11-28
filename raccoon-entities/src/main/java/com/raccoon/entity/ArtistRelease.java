package com.raccoon.entity;

import java.io.Serializable;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.AssociationOverride;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
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
