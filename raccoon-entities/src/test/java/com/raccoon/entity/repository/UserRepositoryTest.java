package com.raccoon.entity.repository;

import com.raccoon.entity.User;

import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.transaction.Transactional;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
class UserRepositoryTest {

    @Inject
    UserRepository repository;

    @Test
    @Transactional
    void findByEmailEmpty() {
        var email = "does not exist";

        assertTrue(repository.findByEmailOptional(email).isEmpty());
    }

    @Test
    @Transactional
    void findByEmailOptional() {
        var email = "email@mail.com";
        var user = new User();
        user.setEmail(email);
        repository.persist(user);

        final var found = repository.findByEmailOptional(email);

        assertTrue(found.isPresent());
        assertEquals(email, found.get().getEmail());
    }

}
