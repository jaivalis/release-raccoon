package com.raccoon.entity;

import java.io.Serializable;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.AssociationOverride;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Setter;
import lombok.ToString;

import static com.raccoon.entity.database.Tables.USER_ARTIST;

//@Data
@ToString
@Entity
@Table(name = USER_ARTIST)
@AssociationOverride(name = "key.raccoonUser", joinColumns = @JoinColumn(name = "user_id"))
@AssociationOverride(name = "key.artist", joinColumns = @JoinColumn(name = "artist_id"))
@Setter
public class UserArtist extends PanacheEntityBase implements Serializable {

    @EmbeddedId
    @JsonbTransient
    public UserArtistPK key = new UserArtistPK();

    @Column
    public Float weight;

    @Column
    public Boolean hasNewRelease = false;

    @JsonbTransient
    public RaccoonUser getUser() {
        return key.getRaccoonUser();
    }

    public void setUser(RaccoonUser raccoonUser) {
        key.setRaccoonUser(raccoonUser);
    }

    @JsonbTransient
    public Artist getArtist() {
        return key.getArtist();
    }

    public void setArtist(Artist artist) {
        key.setArtist(artist);
    }

}
