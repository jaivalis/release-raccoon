package com.raccoon.entity;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
public class User extends PanacheEntity {

    @Column(nullable = false)
    private String name;

    @NotNull
    @Column
    private String email;

    @Column
    private String lastfmUsername;

    @Column
    private Date lastNotified;

//    @ManyToMany
//    @JoinTable(
//            name = "User_Artist",
//            joinColumns = { @JoinColumn(name = "fk_user") },
//            inverseJoinColumns = { @JoinColumn(name = "fk_artist")}
//    )
//    private List<Artist> artists = new ArrayList<>();

    public static Optional<User> findByNameOptional(String name) {
        return Optional.ofNullable(find("name", name).firstResult());
    }
}
