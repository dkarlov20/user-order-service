package com.griddynamics.gridu.user.order.service.service;

import com.griddynamics.gridu.user.order.service.domain.model.User;
import com.griddynamics.gridu.user.order.service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public Mono<User> getUserById(String userId) {
        return userRepository.findById(userId);
    }
}
