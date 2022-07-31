package com.raccoon.entity;

import java.io.Serializable;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.AssociationOverride;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Data;
import lombok.ToString;

import static com.raccoon.entity.database.Tables.USER_ARTIST;

@Data
@ToString
@Entity
@Table(name = USER_ARTIST)
@AssociationOverride(name = "key.user", joinColumns = @JoinColumn(name = "user_id"))
@AssociationOverride(name = "key.artist", joinColumns = @JoinColumn(name = "artist_id"))
public class UserArtist extends PanacheEntityBase implements Serializable {

    @EmbeddedId
    @JsonbTransient
    UserArtistPK key = new UserArtistPK();

    @Column
    Float weight;

    @Column
    Boolean hasNewRelease = false;

    @JsonbTransient
    public User getUser() {
        return key.getUser();
    }

    public void setUser(User user) {
        key.setUser(user);
    }

    @JsonbTransient
    public Artist getArtist() {
        return key.getArtist();
    }

    public void setArtist(Artist artist) {
        key.setArtist(artist);
    }

}
