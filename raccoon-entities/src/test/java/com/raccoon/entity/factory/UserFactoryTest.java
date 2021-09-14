package com.raccoon.entity.factory;

import com.raccoon.entity.User;
import com.raccoon.entity.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    void getOrCreateUserExists() {
        var mail = "user";
        var user = new User();
        when(userRepository.findByEmailOptional(any()))
                .thenReturn(Optional.of(user));

        factory.getOrCreateUser(mail);

        verify(userRepository, times(0)).persist(any(User.class));
    }

    @Test
    void getOrCreateUserCreate() {
        var mail = "user";
        when(userRepository.findByEmailOptional(any()))
                .thenReturn(Optional.empty());

        var created = factory.getOrCreateUser(mail);

        assertEquals(mail, created.getEmail());
        verify(userRepository, times(1)).persist(any(User.class));
    }
}