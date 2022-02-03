package com.griddynamics.gridu.user.order.service.controller;

import com.griddynamics.gridu.user.order.service.domain.dto.UserOrderDto;
import com.griddynamics.gridu.user.order.service.service.UserOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static org.springframework.http.MediaType.APPLICATION_NDJSON_VALUE;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class UserOrderController {
    private final UserOrderService userOrderService;

    @GetMapping(value = "/{userId}", produces = APPLICATION_NDJSON_VALUE)
    public Flux<UserOrderDto> getUserOrders(@PathVariable String userId) {
        return userOrderService.getUserOrdersWithRelevantProducts(userId);
    }
}
