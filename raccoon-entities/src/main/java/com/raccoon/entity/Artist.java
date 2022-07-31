package com.raccoon.entity;

import com.raccoon.common.StringUtil;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;

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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import static com.raccoon.entity.Constants.SPOTIFY_ARTIST_URI_PATTERN;

@Data
@ToString
@Entity
@Table(indexes = {
        @Index(columnList = "spotifyUri")
})
@EqualsAndHashCode
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

}
