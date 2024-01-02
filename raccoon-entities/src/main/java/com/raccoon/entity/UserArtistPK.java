package com.raccoon.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Embeddable
@NoArgsConstructor
public class UserArtistPK implements Serializable {

    @ManyToOne(fetch = FetchType.EAGER)
    RaccoonUser raccoonUser;
    
    @ManyToOne(fetch = FetchType.EAGER)
    Artist artist;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserArtistPK that = (UserArtistPK) o;
        return Objects.equals(artist.id, that.artist.id) && Objects.equals(raccoonUser.id, that.raccoonUser.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(artist.id, raccoonUser.id);
    }
}
