package com.griddynamics.gridu.user.order.service.service;

import com.griddynamics.gridu.user.order.service.domain.dto.UserOrderDto;
import com.griddynamics.gridu.user.order.service.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static java.util.Comparator.comparing;

@Service
@RequiredArgsConstructor
public class UserOrderService {
    private final UserService userService;
    private final OrderSearchService orderSearchService;
    private final ProductInfoService productInfoService;

    public Flux<UserOrderDto> getUserOrdersWithRelevantProducts(String userId) {
        return userService.getUserById(userId)
                .flatMapMany(this::mapOrdersToUser)
                .flatMap(this::mapProductToOrder)
                .sort(comparing(UserOrderDto::getOrderNumber));
    }

    private Flux<UserOrderDto> mapOrdersToUser(User user) {
        return orderSearchService.getUserOrdersByPhoneNumber(user.getPhone())
                .flatMap(order -> Mono.just(UserOrderDto.builder()
                        .userName(user.getName())
                        .phoneNumber(user.getPhone())
                        .orderNumber(order.getOrderNumber())
                        .productCode(order.getProductCode())
                        .build()));
    }

    private Mono<UserOrderDto> mapProductToOrder(UserOrderDto userOrder) {
        return productInfoService.getMostRelevantProductByProductCode(userOrder.getProductCode())
                .map(product -> userOrder.toBuilder()
                        .productName(product.getProductName())
                        .productId(product.getProductId())
                        .build());
    }
}
