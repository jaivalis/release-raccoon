package com.raccoon.entity;

import java.util.*;

import javax.persistence.*;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "not_release")
@Data
@NoArgsConstructor
public class NotRelease extends PanacheEntityBase {

    @Id
    @Column(name = "releaseId")
    public Long id;

    @Column
    String name;

    @Column
    String type;
    //  enum?:
    //    ALBUM("album"),
    //    COMPILATION("compilation"),
    //    SINGLE("single");

    @Column
    String spotifyUri;

//    @Column
//    Date releasedOn;

    @OneToMany(mappedBy = "key.notRelease")
    Set<ArtistRelease> releases = new HashSet<>();

    public static Optional<NotRelease> findByNameOptional(String name) {
        return Optional.ofNullable(find("name", name).firstResult());
    }

    public static Optional<NotRelease> findBySpotifyUri(String uri) {
        return Optional.ofNullable(find("spotifyUri", uri).firstResult());
    }

}
