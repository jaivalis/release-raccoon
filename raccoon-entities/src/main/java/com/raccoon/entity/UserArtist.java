package com.raccoon.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Parameters;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@ToString
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
    private Boolean hasNewRelease = false;

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

    /**
     * For a given collection of artistIds find all UserArtist entries that are referred (by `artist_id`) and set
     * `hasNewRelease` to true.
     * @param artistIds Collection of artistIds.
     * @return a list of updated UserArtist entries.
     */
    public static List<UserArtist> markNewRelease(Collection<Long> artistIds) {
        Stream<UserArtist> stream = streamAll();
        List<UserArtist> collect = stream
                .filter(ua -> artistIds.contains(ua.getArtist().id))
                .peek(userArtist -> userArtist.setHasNewRelease(Boolean.TRUE))
                .collect(Collectors.toList());
        UserArtist.persist(collect);
        return collect;
    }

    public static List<UserArtist> getUserArtistsWithNewReleaseGroupedByArtist() {
//        Stream<UserArtist> stream = find("select ua from UserArtist group by UserArtist.user_id").stream();
        Stream<UserArtist> stream = UserArtist.find("hasNewRelease = :param", Parameters.with("param", "true").map())
                 .project(UserArtist.class).stream();

        return stream.collect(Collectors.toList());
    }

}
