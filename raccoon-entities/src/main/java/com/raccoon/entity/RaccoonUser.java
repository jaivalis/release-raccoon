package com.raccoon.entity;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(exclude = "artists")
@Entity
@Table(
        name = "RaccoonUser", // User is a reserved name from H2 / Quarkus 2.9.x +
        indexes = {
        @Index(name = "email_idx", columnList = "email")
})
public class RaccoonUser extends PanacheEntityBase implements Serializable {

    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotNull
    @Column(unique = true)
    String email;

    @Column
    String username;

    @Column
    String lastfmUsername;

    @Column
    Boolean spotifyEnabled = Boolean.FALSE;

    @Column
    LocalDate lastNotified;

    @CreationTimestamp
    @Column(name = "create_date")
    LocalDateTime createDate;

    @UpdateTimestamp
    @Column(name = "modify_date")
    LocalDateTime modifyDate;

    @Column
    LocalDateTime lastSpotifyScrape = LocalDateTime.now().minusYears(1);

    @Column
    LocalDateTime lastLastFmScrape = LocalDateTime.now().minusYears(1);

    @JsonbTransient
    @OneToMany(mappedBy = "key.raccoonUser", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<UserArtist> artists = new HashSet<>();

    public boolean isLastfmScrapeRequired(int scrapeIntervalDays) {
        return lastLastFmScrape == null || !isLastLastfmScrapeLt(scrapeIntervalDays, lastLastFmScrape);
    }

    public boolean isSpotifyScrapeRequired(int scrapeIntervalDays) {
        return lastSpotifyScrape == null || !isLastSpotifyScrapeLt(scrapeIntervalDays, lastSpotifyScrape);
    }

////////////////////////////////////////////////////////////////////////////////////////////////////

    private boolean isLastLastfmScrapeLt(int days, LocalDateTime lastLastFmScrape) {
        return ChronoUnit.DAYS.between(lastLastFmScrape, LocalDateTime.now()) < days;
    }

    private boolean isLastSpotifyScrapeLt(int days, LocalDateTime lastSpotifyScrape) {
        return ChronoUnit.DAYS.between(lastSpotifyScrape, LocalDateTime.now()) < days;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
