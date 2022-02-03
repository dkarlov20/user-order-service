package com.griddynamics.gridu.user.order.service.service;

import com.griddynamics.gridu.user.order.service.domain.model.User;
import com.griddynamics.gridu.user.order.service.repository.UserRepository;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    private static final EasyRandom EASY_RANDOM = new EasyRandom();
    private static final String USER_ID = "user1";

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    public void getUserByIdTest() {
        User expectedUser = EASY_RANDOM.nextObject(User.class);

        when(userRepository.findById(USER_ID))
                .thenReturn(Mono.just(expectedUser));

        Mono<User> actualUser = userService.getUserById(USER_ID);

        StepVerifier.create(actualUser)
                .expectNext(expectedUser)
                .verifyComplete();

        verify(userRepository).findById(USER_ID);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void getUserByIdWhenUserNotFoundTest() {

        when(userRepository.findById(USER_ID))
                .thenReturn(Mono.empty());

        Mono<User> actualUser = userService.getUserById(USER_ID);

        StepVerifier.create(actualUser)
                .expectNextCount(0)
                .verifyComplete();

        verify(userRepository).findById(USER_ID);
        verifyNoMoreInteractions(userRepository);
    }
}