package com.raccoon.entity;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
public class User extends PanacheEntity {

    @Column
    private String name;

    @Column
    private String email;

    @Column
    private String lastfmUsername;

    @Column
    private Date lastNotified;

    @ManyToMany
    @JoinTable(
            name = "user_artist",
            joinColumns = { @JoinColumn(name = "fk_user") },
            inverseJoinColumns = { @JoinColumn(name = "fk_artist")}
    )
    private List<Artist> artists = new ArrayList<>();

    
}
