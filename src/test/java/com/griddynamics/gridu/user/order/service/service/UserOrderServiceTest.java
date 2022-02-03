package com.griddynamics.gridu.user.order.service.service;

import com.griddynamics.gridu.user.order.service.domain.dto.OrderDto;
import com.griddynamics.gridu.user.order.service.domain.dto.ProductDto;
import com.griddynamics.gridu.user.order.service.domain.dto.UserOrderDto;
import com.griddynamics.gridu.user.order.service.domain.model.User;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserOrderServiceTest {
    private static final EasyRandom EASY_RANDOM = new EasyRandom();
    private static final String USER_ID = "user1";
    private static final int ORDERS_AMOUNT = 5;
    private static final double DEFAULT_DOUBLE = 0.0;
    private static final ProductDto DEFAULT_PRODUCT = new ProductDto(EMPTY, EMPTY, EMPTY, DEFAULT_DOUBLE);

    @Mock
    private UserService userService;

    @Mock
    private OrderSearchService orderSearchService;

    @Mock
    private ProductInfoService productInfoService;

    @InjectMocks
    private UserOrderService userOrderService;

    @Test
    public void getUserOrdersWithRelevantProductsTest() {
        User expectedUser = EASY_RANDOM.nextObject(User.class);
        List<OrderDto> expectedOrders = EASY_RANDOM.objects(OrderDto.class, ORDERS_AMOUNT)
                .collect(toList());
        List<Tuple2<OrderDto, ProductDto>> relevantProductByOrder = expectedOrders.stream()
                .map(order -> Tuples.of(order, EASY_RANDOM.nextObject(ProductDto.class)))
                .collect(toList());
        List<UserOrderDto> expectedUserOrders = relevantProductByOrder.stream()
                .map(tuple -> UserOrderDto.builder()
                        .orderNumber(tuple.getT1().getOrderNumber())
                        .userName(expectedUser.getName())
                        .phoneNumber(expectedUser.getPhone())
                        .productCode(tuple.getT1().getProductCode())
                        .productName(tuple.getT2().getProductName())
                        .productId(tuple.getT2().getProductName())
                        .build())
                .collect(toList());

        when(userService.getUserById(USER_ID))
                .thenReturn(Mono.just(expectedUser));
        when(orderSearchService.getUserOrdersByPhoneNumber(expectedUser.getPhone()))
                .thenReturn(Flux.fromIterable(expectedOrders));
        for (Tuple2<OrderDto, ProductDto> tuple : relevantProductByOrder) {
            when(productInfoService.getMostRelevantProductByProductCode(tuple.getT1().getProductCode()))
                    .thenReturn(Mono.just(tuple.getT2()));
        }

        Flux<UserOrderDto> actualUserOrders = userOrderService.getUserOrdersWithRelevantProducts(USER_ID);

        StepVerifier.create(actualUserOrders)
                .expectNextCount(ORDERS_AMOUNT)
                .thenConsumeWhile(expectedUserOrders::contains)
                .verifyComplete();

        verify(userService).getUserById(USER_ID);
        verify(orderSearchService).getUserOrdersByPhoneNumber(expectedUser.getPhone());
        for (Tuple2<OrderDto, ProductDto> tuple : relevantProductByOrder) {
            verify(productInfoService).getMostRelevantProductByProductCode(tuple.getT1().getProductCode());
        }

        verifyNoMoreInteractions(userService, orderSearchService, productInfoService);
    }

    @Test
    public void getUserOrdersWithRelevantProductsWhenUserNotFoundTest() {
        when(userService.getUserById(USER_ID))
                .thenReturn(Mono.empty());

        Flux<UserOrderDto> actualUserOrders = userOrderService.getUserOrdersWithRelevantProducts(USER_ID);

        StepVerifier.create(actualUserOrders)
                .expectNextCount(0)
                .verifyComplete();

        verify(userService).getUserById(USER_ID);

        verifyNoMoreInteractions(userService, orderSearchService, productInfoService);
    }

    @Test
    public void getUserOrdersWithRelevantProductsWhenOrdersNotFoundTest() {
        User expectedUser = EASY_RANDOM.nextObject(User.class);

        when(userService.getUserById(USER_ID))
                .thenReturn(Mono.just(expectedUser));
        when(orderSearchService.getUserOrdersByPhoneNumber(expectedUser.getPhone()))
                .thenReturn(Flux.empty());

        Flux<UserOrderDto> actualUserOrders = userOrderService.getUserOrdersWithRelevantProducts(USER_ID);

        StepVerifier.create(actualUserOrders)
                .expectNextCount(0)
                .verifyComplete();

        verify(userService).getUserById(USER_ID);
        verify(orderSearchService).getUserOrdersByPhoneNumber(expectedUser.getPhone());

        verifyNoMoreInteractions(userService, orderSearchService, productInfoService);
    }

    @Test
    public void getUserOrdersWithRelevantProductsWhenProductsNotFoundTest() {
        User expectedUser = EASY_RANDOM.nextObject(User.class);
        List<OrderDto> expectedOrders = EASY_RANDOM.objects(OrderDto.class, ORDERS_AMOUNT)
                .collect(toList());
        List<UserOrderDto> expectedUserOrders = expectedOrders.stream()
                .map(order -> UserOrderDto.builder()
                        .orderNumber(order.getOrderNumber())
                        .userName(expectedUser.getName())
                        .phoneNumber(expectedUser.getPhone())
                        .productCode(order.getProductCode())
                        .productName(EMPTY)
                        .productId(EMPTY)
                        .build())
                .collect(toList());

        when(userService.getUserById(USER_ID))
                .thenReturn(Mono.just(expectedUser));
        when(orderSearchService.getUserOrdersByPhoneNumber(expectedUser.getPhone()))
                .thenReturn(Flux.fromIterable(expectedOrders));
        for (OrderDto order : expectedOrders) {
            when(productInfoService.getMostRelevantProductByProductCode(order.getProductCode()))
                    .thenReturn(Mono.just(DEFAULT_PRODUCT));
        }

        Flux<UserOrderDto> actualUserOrders = userOrderService.getUserOrdersWithRelevantProducts(USER_ID);

        StepVerifier.create(actualUserOrders)
                .expectNextCount(ORDERS_AMOUNT)
                .thenConsumeWhile(expectedUserOrders::contains)
                .verifyComplete();

        verify(userService).getUserById(USER_ID);
        verify(orderSearchService).getUserOrdersByPhoneNumber(expectedUser.getPhone());
        for (OrderDto order : expectedOrders) {
            verify(productInfoService).getMostRelevantProductByProductCode(order.getProductCode());
        }

        verifyNoMoreInteractions(userService, orderSearchService, productInfoService);
    }
}