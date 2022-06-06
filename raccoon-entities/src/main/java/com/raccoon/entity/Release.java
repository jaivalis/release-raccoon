package com.raccoon.entity;

import com.raccoon.common.StringUtil;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
import lombok.NoArgsConstructor;
import lombok.ToString;

import static com.raccoon.entity.Constants.SPOTIFY_RELEASE_URI_PATTERN;

@Data
@Entity(name = "Releases")
@Table(indexes = {
        @Index(columnList = "spotifyUri")
})
@NoArgsConstructor
public class Release extends PanacheEntityBase implements Serializable {

    @Id
    @Column(name = "releaseId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(length = 300)
    String name;

    @Column
    String type;

    @Column
    String spotifyUri;

    @Column
    String musicbrainzId;

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

    @JsonbTransient
    public String getSpotifyUriId() {
        if (StringUtil.isNullOrEmpty(spotifyUri)) {
            return "";
        }

        if (!SPOTIFY_RELEASE_URI_PATTERN.matcher(spotifyUri).matches()) {
            return "";
        }

        String[] parts = spotifyUri.split(":");
        return parts[parts.length - 1];
    }

}
