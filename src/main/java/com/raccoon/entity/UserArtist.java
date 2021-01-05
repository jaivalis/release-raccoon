package com.raccoon.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table
@AssociationOverride(name = "key.user", joinColumns = @JoinColumn(name = "user_id"))
@AssociationOverride(name = "key.artist", joinColumns = @JoinColumn(name = "artist_id"))
public class UserArtist extends PanacheEntityBase implements Serializable {

    @EmbeddedId
    @JsonIgnore
    UserArtistPK key = new UserArtistPK();

    @Column
    private Float weight;

    @JsonIgnore
    public User getUser() {
        return key.getUser();
    }

    public void setUser(User user) {
        key.setUser(user);
    }

    @JsonIgnore
    public Artist getArtist() {
        return key.getArtist();
    }

    public void setArtist(Artist artist) {
        key.setArtist(artist);
    }

}
