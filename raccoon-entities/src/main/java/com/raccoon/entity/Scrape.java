package com.raccoon.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


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

    @Transient
    @Setter
    List<Release> releases = new ArrayList<>();

}
