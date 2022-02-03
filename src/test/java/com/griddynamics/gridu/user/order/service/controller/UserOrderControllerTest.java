package com.griddynamics.gridu.user.order.service.controller;

import com.griddynamics.gridu.user.order.service.domain.dto.UserOrderDto;
import com.griddynamics.gridu.user.order.service.service.UserOrderService;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = UserOrderController.class)
public class UserOrderControllerTest {
    private static final EasyRandom EASY_RANDOM = new EasyRandom();
    private static final int NUMBER_OF_ORDERS = 5;
    private static final String USER_ID = "user1";
    private static final String ORDERS_ENDPOINT = "/orders/";

    @MockBean
    private UserOrderService userOrderService;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void getUserOrdersTest() {
        List<UserOrderDto> userOrders = EASY_RANDOM.objects(UserOrderDto.class, NUMBER_OF_ORDERS)
                .collect(toList());

        when(userOrderService.getUserOrdersWithRelevantProducts(USER_ID))
                .thenReturn(Flux.fromIterable(userOrders));

        webTestClient.get()
                .uri(ORDERS_ENDPOINT + USER_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserOrderDto.class)
                .hasSize(NUMBER_OF_ORDERS)
                .isEqualTo(userOrders);

        verify(userOrderService).getUserOrdersWithRelevantProducts(USER_ID);
        verifyNoMoreInteractions(userOrderService);
    }
}