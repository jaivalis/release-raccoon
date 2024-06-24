package com.raccoon.entity;

import com.raccoon.common.StringUtil;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import static com.raccoon.entity.Constants.SPOTIFY_ARTIST_URI_PATTERN;

@ToString
@Entity
@Table(indexes = {
        @Index(name = "ArtistSpotifyUri_idx", columnList = "spotifyUri"),
        @Index(name = "ArtistName_idx", columnList = "name")
})
@Getter
@Setter
@NoArgsConstructor
@Indexed
public class Artist extends PanacheEntityBase implements Serializable {

    @Id
    @Column(name = "artistId")
    @SequenceGenerator(
            name = "ART_SEQ",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @CreationTimestamp
    @Column(name = "create_date")
    LocalDateTime createDate;

    @Column(length = 300)
    @FullTextField(analyzer = "name")
    String name;

    @Column
    String lastfmUri;

    @Column
    String spotifyUri;

    @Column
    String musicbrainzId;

    @ToString.Exclude
    @JsonbTransient
    @OneToMany(mappedBy = "key.artist", cascade = CascadeType.ALL)
    private Set<ArtistRelease> releases = new HashSet<>();

    @ToString.Exclude
    @JsonbTransient
    @OneToMany(mappedBy = "key.artist", cascade = CascadeType.ALL)
    private Set<UserArtist> users = new HashSet<>();

    public String getSpotifyUriId() {
        if (StringUtil.isNullOrEmpty(spotifyUri)) {
            return "";
        }

        if (!SPOTIFY_ARTIST_URI_PATTERN.matcher(spotifyUri).matches()) {
            return "";
        }

        String[] parts = spotifyUri.split(":");
        return parts[parts.length - 1];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Artist artist = (Artist) o;
        return Objects.equals(id, artist.id) && Objects.equals(name, artist.name);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(name);
        return result;
    }

}
