package com.raccoon.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Entity(name = "Releases")
@Table(indexes = {
        @Index(columnList = "spotifyUri")
})
@NoArgsConstructor
@RegisterForReflection
public class Release extends PanacheEntityBase implements Serializable {

    @Id
    @Column(name = "releaseId")
    @GeneratedValue
    public Long id;

    @Column
    String name;

    @Column
    String type;

    @Column(unique = true)
    String spotifyUri;

    @Column
    LocalDate releasedOn;

    @ToString.Exclude
    @JsonbTransient
    @OneToMany(mappedBy = "key.release", cascade = CascadeType.ALL)
    private List<ArtistRelease> releases = new ArrayList<>();

    @JsonbTransient
    public List<Artist> getArtists() {
        return releases.stream()
                .map(ArtistRelease::getArtist)
                .toList();
    }

}
