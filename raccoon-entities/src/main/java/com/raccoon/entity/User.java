package com.raccoon.entity;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(exclude = "artists")
@Entity
@Table(indexes = {
        @Index(columnList = "email")
})
public class User extends PanacheEntity implements Serializable {

    @NotNull
    @Column(unique = true)
    private String email;

    @Column
    private String lastfmUsername;

    @Column
    private Boolean spotifyEnabled;

    @Column
    private LocalDate lastNotified;

    @CreationTimestamp
    @Column(name = "create_date")
    private LocalDateTime createDate;

    @UpdateTimestamp
    @Column(name = "modify_date")
    private LocalDateTime modifyDate;

    @Column
    private LocalDateTime lastSpotifyScrape;

    @Column
    private LocalDateTime lastLastFmScrape;

    @JsonbTransient
    @OneToMany(mappedBy = "key.user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<UserArtist> artists = new HashSet<>();

    public static Optional<User> findByIdOptional(Long id) {
        return Optional.ofNullable(find("id", id).firstResult());
    }

    public static Optional<User> findByEmailOptional(String email) {
        return Optional.ofNullable(find("email", email).firstResult());
    }

    public boolean isLastfmScrapeRequired(int scrapeIntervalDays) {
        return lastLastFmScrape == null || isLastLastfmScrapeLt(scrapeIntervalDays);
    }

    public boolean isSpotifyScrapeRequired(int scrapeIntervalDays) {
        return lastSpotifyScrape == null || isLastSpotifyScrapeLt(scrapeIntervalDays);
    }

////////////////////////////////////////////////////////////////////////////////////////////////////

    private boolean isLastLastfmScrapeLt(int days) {
        return ChronoUnit.DAYS.between(getLastLastFmScrape(), LocalDateTime.now()) < days;
    }

    private boolean isLastSpotifyScrapeLt(int days) {
        return ChronoUnit.DAYS.between(getLastSpotifyScrape(), LocalDateTime.now()) < days;
    }

}
