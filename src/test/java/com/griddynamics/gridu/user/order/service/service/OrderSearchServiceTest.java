package com.griddynamics.gridu.user.order.service.service;

import com.griddynamics.gridu.user.order.service.configuration.OrderSearchServiceProperties;
import com.griddynamics.gridu.user.order.service.domain.dto.OrderDto;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderSearchServiceTest {

    private static final EasyRandom EASY_RANDOM = new EasyRandom();
    private static final int NUMBER_OF_ORDERS = 5;
    private static final String PHONE_NUMBER_PARAMETER = "phoneNumber";
    private static final String PHONE_NUMBER = "123";
    private static final String HOST = "http://localhost";
    private static final int PORT = 8080;
    private static final String USER_ORDERS_ENDPOINT = "/orderSearchService/order/phone";
    private static final String EXPECTED_URL = UriComponentsBuilder.fromHttpUrl(HOST)
            .port(PORT)
            .path(USER_ORDERS_ENDPOINT)
            .queryParam(PHONE_NUMBER_PARAMETER, PHONE_NUMBER)
            .toUriString();

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private OrderSearchServiceProperties orderSearchServiceProperties;

    @InjectMocks
    private OrderSearchService orderSearchService;

    @Test
    public void getUserOrdersByPhoneNumberTest() {
        List<OrderDto> expectedOrders = EASY_RANDOM.objects(OrderDto.class, NUMBER_OF_ORDERS)
                .collect(toList());

        when(orderSearchServiceProperties.getHost())
                .thenReturn(HOST);
        when(orderSearchServiceProperties.getPort())
                .thenReturn(PORT);
        when(orderSearchServiceProperties.getOrdersByPhoneEndpoint())
                .thenReturn(USER_ORDERS_ENDPOINT);
        when(webClient.get())
                .thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(EXPECTED_URL))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve())
                .thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(OrderDto.class))
                .thenReturn(Flux.fromIterable(expectedOrders));

        Flux<OrderDto> actualOrders = orderSearchService.getUserOrdersByPhoneNumber(PHONE_NUMBER);

        StepVerifier.create(actualOrders)
                .expectNextCount(NUMBER_OF_ORDERS)
                .thenConsumeWhile(expectedOrders::contains)
                .verifyComplete();

        verify(orderSearchServiceProperties).getHost();
        verify(orderSearchServiceProperties).getPort();
        verify(orderSearchServiceProperties).getOrdersByPhoneEndpoint();
        verify(webClient).get();
        verify(requestHeadersUriSpec).uri(EXPECTED_URL);
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToFlux(OrderDto.class);

        verifyNoMoreInteractions(webClient, requestHeadersUriSpec, requestHeadersSpec, responseSpec);
    }

    @Test
    public void getUserOrdersByPhoneNumberWithServerErrorTest() {
        Throwable mockThrowable = mock(Throwable.class);

        when(orderSearchServiceProperties.getHost())
                .thenReturn(HOST);
        when(orderSearchServiceProperties.getPort())
                .thenReturn(PORT);
        when(orderSearchServiceProperties.getOrdersByPhoneEndpoint())
                .thenReturn(USER_ORDERS_ENDPOINT);
        when(webClient.get())
                .thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(EXPECTED_URL))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve())
                .thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(OrderDto.class))
                .thenReturn(Flux.error(mockThrowable));

        Flux<OrderDto> actualOrders = orderSearchService.getUserOrdersByPhoneNumber(PHONE_NUMBER);

        StepVerifier.create(actualOrders)
                .expectNextCount(0)
                .verifyComplete();

        verify(orderSearchServiceProperties).getHost();
        verify(orderSearchServiceProperties).getPort();
        verify(orderSearchServiceProperties).getOrdersByPhoneEndpoint();
        verify(webClient).get();
        verify(requestHeadersUriSpec).uri(EXPECTED_URL);
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToFlux(OrderDto.class);

        verifyNoMoreInteractions(webClient, requestHeadersUriSpec, requestHeadersSpec, responseSpec);
    }
}