package com.raccoon.entity;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OrderColumn;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;


@Entity
@Getter
public class Scrape extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @CreationTimestamp
    @Column(name = "create_date")
    LocalDateTime createDate;

    @UpdateTimestamp
    @Column(name = "modify_date")
    LocalDateTime modifyDate;

    @Setter
    @OrderColumn
    LocalDateTime completeDate;

    @Column
    @Setter
    Long releasesFromSpotify;

    @Column
    @Setter
    Long releasesFromMusicbrainz;

    @Column
    @Setter
    Integer releaseCount;

    @Column
    @Setter
    Long usersNotified;

    @Column
    @Setter
    Long relevantReleases;

    @Column
    @Setter
    Boolean isComplete;

}
