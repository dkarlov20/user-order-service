package com.griddynamics.gridu.user.order.service.service.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.griddynamics.gridu.user.order.service.configuration.OrderSearchServiceProperties;
import com.griddynamics.gridu.user.order.service.domain.dto.OrderDto;
import com.griddynamics.gridu.user.order.service.service.OrderSearchService;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.util.stream.Collectors.toList;

@WireMockTest(httpPort = 8081)
@SpringBootTest
@ActiveProfiles("test")
public class OrderSearchServiceIntegrationTest {

    private static final EasyRandom EASY_RANDOM = new EasyRandom();
    private static final int NUMBER_OF_ORDERS = 5;
    private static final String PHONE_NUMBER_PARAMETER = "phoneNumber";
    private static final String PHONE_NUMBER = "123";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderSearchService orderSearchService;

    @Autowired
    private OrderSearchServiceProperties orderSearchServiceProperties;

    @Test
    public void getUserOrdersByPhoneNumberTest(WireMockRuntimeInfo wireMockRuntimeInfo) throws JsonProcessingException {
        List<OrderDto> expectedOrders = EASY_RANDOM.objects(OrderDto.class, NUMBER_OF_ORDERS)
                .collect(toList());
        String ordersJson = objectMapper.writeValueAsString(expectedOrders);

        wireMockRuntimeInfo.getWireMock()
                .register(get(urlPathEqualTo(orderSearchServiceProperties.getOrdersByPhoneEndpoint()))
                        .withQueryParam(PHONE_NUMBER_PARAMETER, equalTo(PHONE_NUMBER))
                        .willReturn(aResponse()
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_NDJSON_VALUE)
                                .withBody(ordersJson)));

        Flux<OrderDto> actualOrders = orderSearchService.getUserOrdersByPhoneNumber(PHONE_NUMBER);

        StepVerifier.create(actualOrders)
                .expectNextCount(NUMBER_OF_ORDERS)
                .thenConsumeWhile(expectedOrders::contains)
                .verifyComplete();
    }

    @Test
    public void getUserOrdersByPhoneNumberWithServerErrorTest(WireMockRuntimeInfo wireMockRuntimeInfo) {
        wireMockRuntimeInfo.getWireMock()
                .register(get(urlPathEqualTo(orderSearchServiceProperties.getOrdersByPhoneEndpoint()))
                        .withQueryParam(PHONE_NUMBER_PARAMETER, equalTo(PHONE_NUMBER))
                        .willReturn(serverError()));

        Flux<OrderDto> actualOrders = orderSearchService.getUserOrdersByPhoneNumber(PHONE_NUMBER);

        StepVerifier.create(actualOrders)
                .expectNextCount(0)
                .verifyComplete();
    }
}