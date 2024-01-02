package com.raccoon.entity.repository;

import com.raccoon.entity.RaccoonUser;

import java.util.Optional;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class UserRepository implements PanacheRepository<RaccoonUser> {

    public Optional<RaccoonUser> findByEmailOptional(String email) {
        return Optional.ofNullable(find("email", email).firstResult());
    }

    public RaccoonUser findByEmail(String email) {
        return findByEmailOptional(email)
                .orElseThrow(() -> new NotFoundException("RaccoonUser not found"));
    }

}
