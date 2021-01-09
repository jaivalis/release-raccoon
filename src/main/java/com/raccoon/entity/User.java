package com.raccoon.entity;


import io.quarkus.hibernate.orm.panache.PanacheEntity;
import lombok.Data;
import lombok.ToString;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Data
@Entity
@Table(indexes = {
        @Index(columnList = "email")
})
@ToString(exclude = "artists")
public class User extends PanacheEntity implements Serializable {

    @NotNull
    @Column(unique = true)
    private String email;

    @Column
    private String lastfmUsername;

    @Column
    private LocalDate lastNotified;

    @JsonbTransient
    @OneToMany(mappedBy = "key.user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<UserArtist> artists = new HashSet<>();

    public static Optional<User> findbyEmailOptional(String email) {
        return Optional.ofNullable(find("email", email).firstResult());
    }
}
