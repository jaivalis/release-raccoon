package com.raccoon.entity.repository;

import com.raccoon.entity.User;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.NotFoundException;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

    public Optional<User> findByEmailOptional(String email) {
        return Optional.ofNullable(find("email", email).firstResult());
    }

    public User findByEmail(String email) {
        return findByEmailOptional(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

}
