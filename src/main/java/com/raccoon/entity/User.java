package com.raccoon.entity;


import io.quarkus.hibernate.orm.panache.PanacheEntity;
import lombok.Data;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Data
@Entity
public class User extends PanacheEntity implements Serializable {

    @NotNull
    @Column(unique = true)
    private String email;

    @Column
    private String lastfmUsername;

    @Column
    private LocalDate lastNotified;

    @JsonbTransient
    @OneToMany(mappedBy = "key.user", cascade = CascadeType.ALL)
    private Set<UserArtist> artists = new HashSet<>();

    public static Optional<User> findbyEmailOptional(String email) {
        return Optional.ofNullable(find("email", email).firstResult());
    }
}
