package com.raccoon.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.util.Objects;

@Data
@Embeddable
@NoArgsConstructor
public class ArtistReleaseKey implements Serializable {

    @ManyToOne(fetch = FetchType.EAGER)
    private Artist artist;

    @ManyToOne(fetch = FetchType.EAGER)
    private Release release;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArtistReleaseKey that = (ArtistReleaseKey) o;
        return Objects.equals(artist.id, that.artist.id) && Objects.equals(release.id, that.release.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(artist.id, release.id);
    }
}
