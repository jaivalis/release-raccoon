package com.raccoon.entity.repository;

import com.raccoon.entity.factory.UserFactory;

import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.NotFoundException;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
class UserRepositoryTest {

    @Inject
    UserRepository repository;

    @Inject
    UserFactory factory;

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
        factory.getOrCreateUser(email);

        final var found = repository.findByEmailOptional(email);

        assertTrue(found.isPresent());
        assertEquals(email, found.get().getEmail());
    }

    @Test
    @Transactional
    void findByEmailNotFound() {
        var email = "does not exist";

        assertThrows(NotFoundException.class, () -> repository.findByEmail(email));
    }

    @Test
    @Transactional
    void findByEmail() {
        var email = "email@mail.com";
        factory.getOrCreateUser(email);

        final var found = repository.findByEmail(email);

        assertEquals(email, found.getEmail());
    }

}
