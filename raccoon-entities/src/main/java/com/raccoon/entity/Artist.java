package com.raccoon.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @CreationTimestamp
    @Column(name = "create_date")
    LocalDateTime createDate;

    @Column
    String name;

    @Column
    String lastfmUri;

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

}
