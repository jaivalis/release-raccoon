package com.raccoon.entity;


import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
public class Artist extends PanacheEntity {

    @Column
    private String name;

    @Column
    private String spotifyUri;

    @ManyToMany(mappedBy = "artists")
    private List<User> users = new ArrayList<>();

}
