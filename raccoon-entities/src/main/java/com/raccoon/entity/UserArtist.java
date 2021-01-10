package com.raccoon.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.quarkus.hibernate.orm.panache.PanacheEntityBase.persist;
import static io.quarkus.hibernate.orm.panache.PanacheEntityBase.streamAll;

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

    @Column
    private Boolean hasNewRelease;

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

    public static List<UserArtist> markNewRelease(Collection<Long> artistIds) {
        Stream<UserArtist> stream = streamAll();
        List<UserArtist> changed = stream
                .filter(ua -> artistIds.contains(ua.getArtist().id))
                .peek(userArtist -> userArtist.setHasNewRelease(true))
                .collect(Collectors.toList());
        persist(changed);
        return changed;
    }

}
