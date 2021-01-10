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
public class UserArtistPK implements Serializable {

    @ManyToOne(fetch = FetchType.EAGER)
    private User user;
    
    @ManyToOne(fetch = FetchType.EAGER)
    private Artist artist;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserArtistPK that = (UserArtistPK) o;
        return Objects.equals(artist.id, that.artist.id) && Objects.equals(user.id, that.user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(artist.id, user.id);
    }
}
