package com.raccoon.entity.factory;

import com.raccoon.entity.User;
import com.raccoon.entity.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserFactoryTest {

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
        var user = new User();

        factory.createUser(mail);

        verify(userRepository, times(0)).persist(user);
    }

}