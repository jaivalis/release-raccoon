package com.raccoon.entity;

import java.io.Serializable;
import java.util.*;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.*;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.*;

@Data
@Entity
@NoArgsConstructor
public class Artist extends PanacheEntityBase implements Serializable {

    @Id
    @Column(name = "artistId")
    @GeneratedValue
    public Long id;

    @Column
    String name;

    @Column
    String spotifyUri;

    @JsonbTransient
    @OneToMany(mappedBy = "key.artist", cascade = CascadeType.ALL)
    private Set<ArtistRelease> releases = new HashSet<>();

    public static Artist findByName(String name) {
        return find("name", name).firstResult();
    }

    public static Optional<Artist> findByNameOptional(String name) {
        return Optional.ofNullable(find("name", name).firstResult());
    }

}
