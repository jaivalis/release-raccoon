package com.raccoon.entity.factory;

import com.raccoon.entity.RaccoonUser;
import com.raccoon.entity.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.quarkus.test.TestTransaction;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@TestTransaction
class RaccoonUserFactoryTest {

    UserFactory factory;

    @Mock
    UserRepository userRepository;

    @BeforeEach
    void setUp() {
        factory = new UserFactory(userRepository);
    }

    @Test
    void createUser() {
        var mail = "user";
        var user = new RaccoonUser();

        factory.createUser(mail);

        verify(userRepository, times(0)).persist(user);
    }

}