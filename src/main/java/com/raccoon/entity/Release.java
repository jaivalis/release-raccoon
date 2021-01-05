package com.raccoon.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.*;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@Entity(name = "Releases")
@NoArgsConstructor
public class Release extends PanacheEntityBase implements Serializable {

    @Id
    @Column(name = "releaseId")
    @GeneratedValue
    public Long id;

    @Column
    String name;

    @Column
    String type;
    //  enum?:
    //    ALBUM("album"),
    //    COMPILATION("compilation"),
    //    SINGLE("single");

    @Column(unique = true)
    String spotifyUri;

    @Column
    LocalDate releasedOn;

    @JsonbTransient
    @OneToMany(mappedBy = "key.release", cascade = CascadeType.ALL)
    List<ArtistRelease> releases = new ArrayList<>();

    public static Optional<Release> findByNameOptional(String name) {
        return Optional.ofNullable(find("name", name).firstResult());
    }

    public static Optional<Release> findBySpotifyUriOptional(String uri) {
        return Optional.ofNullable(find("spotifyUri", uri).firstResult());
    }

}
