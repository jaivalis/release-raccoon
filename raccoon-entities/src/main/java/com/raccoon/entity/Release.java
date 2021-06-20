package com.raccoon.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Entity(name = "Releases")
@Table(indexes = {
        @Index(columnList = "spotifyUri")
})
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

    @Column(unique = true)
    String spotifyUri;

    @Column
    LocalDate releasedOn;

    @ToString.Exclude
    @JsonbTransient
    @OneToMany(mappedBy = "key.release", cascade = CascadeType.ALL)
    private List<ArtistRelease> releases = new ArrayList<>();

    public List<Artist> getArtists() {
        return releases.stream()
                .map(ArtistRelease::getArtist)
                .collect(Collectors.toList());
    }

    public static Optional<Release> findByNameOptional(String name) {
        return Optional.ofNullable(find("name", name).firstResult());
    }

    public static Optional<Release> findBySpotifyUriOptional(String uri) {
        return Optional.ofNullable(find("spotifyUri", uri).firstResult());
    }

    public static List<Release> findByArtistsSinceDays(Collection<Artist> artists, int days) {
        LocalDate leastDate = LocalDate.now().minusDays(days);

        return Release.list("releasedOn > ?1", leastDate)
                .stream()
                .map(Release.class::cast)
                .filter(release -> release.getArtists().stream().anyMatch(artists::contains))
                .collect(Collectors.toList());

    }

}
