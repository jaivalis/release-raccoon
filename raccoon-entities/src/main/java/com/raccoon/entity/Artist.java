package com.raccoon.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@Entity
@Table(indexes = {
        @Index(columnList = "spotifyUri")
})
@EqualsAndHashCode
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

    @JsonIgnore
    @ToString.Exclude
    @JsonbTransient
    @OneToMany(mappedBy = "key.artist", cascade = CascadeType.ALL)
    private Set<ArtistRelease> releases = new HashSet<>();

    @JsonIgnore
    @ToString.Exclude
    @JsonbTransient
    @OneToMany(mappedBy = "key.artist", cascade = CascadeType.ALL)
    private Set<UserArtist> users = new HashSet<>();

    public static Artist findByName(String name) {
        return find("name", name).firstResult();
    }

    public static Optional<Artist> findByNameOptional(String name) {
        return Optional.ofNullable(find("name", name).firstResult());
    }

}
