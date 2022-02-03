package com.griddynamics.gridu.user.order.service.repository;

import com.griddynamics.gridu.user.order.service.domain.model.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends ReactiveMongoRepository<User, String> {
}
