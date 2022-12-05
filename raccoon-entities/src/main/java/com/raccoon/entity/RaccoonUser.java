package com.raccoon.entity;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
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
    LocalDateTime lastSpotifyScrape = LocalDateTime.MIN;

    @Column
    LocalDateTime lastLastFmScrape = LocalDateTime.MIN;

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

}
