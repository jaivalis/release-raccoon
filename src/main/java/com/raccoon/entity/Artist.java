package com.raccoon.entity;

import java.util.*;

import javax.persistence.*;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.*;

@Data
@Entity
@NoArgsConstructor
public class Artist extends PanacheEntityBase {

    @Id
    @Column(name = "artistId")
    public Long id;

    @Column
    String name;

    @Column
    String spotifyUri;

//    @ManyToMany(mappedBy = "artists")
//    List<User> users = new ArrayList<>();

//    @ManyToMany
//    @JoinTable(
//            name = "User_Artist",
//            joinColumns = { @JoinColumn(name = "fk_user") },
//            inverseJoinColumns = { @JoinColumn(name = "fk_artist")}
//    )
    @OneToMany(mappedBy = "key.artist")
    private Set<ArtistRelease> releases = new HashSet<>();

    public static Artist findByName(String name) {
        return find("name", name).firstResult();
    }

    public static Optional<Artist> findByNameOptional(String name) {
        return Optional.ofNullable(find("name", name).firstResult());
    }


}

