package com.raccoon.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ManyToMany;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

public class Release extends PanacheEntity {

    @Column
    private String name;

    @Column
    private String releaseType;

    @Column
    private String spotifyUri;

    @Column
    private Date releasedOn;

    @ManyToMany(mappedBy = "releases")
    private List<User> users = new ArrayList<>();

    @ManyToMany(mappedBy = "releases")
    private List<Artist> artists = new ArrayList<>();

}
