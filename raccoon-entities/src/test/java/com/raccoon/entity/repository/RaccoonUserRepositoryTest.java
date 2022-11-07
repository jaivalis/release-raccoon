package com.raccoon.entity.repository;

import com.raccoon.entity.factory.UserFactory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.NotFoundException;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
class RaccoonUserRepositoryTest {

    @Inject
    UserRepository repository;

    @Inject
    UserFactory factory;

    @AfterEach
    @Transactional
    void setup() {
        repository.deleteAll();
    }

    @Test
    @TestTransaction
    void findByEmailEmpty() {
        var email = "does not exist";

        assertTrue(repository.findByEmailOptional(email).isEmpty());
    }

    @Test
    @TestTransaction
    void findByEmailOptional() {
        var email = "email@mail.com";
        factory.createUser(email);

        final var found = repository.findByEmailOptional(email);

        assertTrue(found.isPresent());
        assertEquals(email, found.get().getEmail());
    }

    @Test
    @TestTransaction
    void findByEmailNotFound() {
        var email = "does not exist";

        assertThrows(NotFoundException.class, () -> repository.findByEmail(email));
    }

    @Test
    @TestTransaction
    void findByEmail() {
        var email = "email@mail.com";
        factory.createUser(email);

        final var found = repository.findByEmail(email);

        assertEquals(email, found.getEmail());
    }

}
