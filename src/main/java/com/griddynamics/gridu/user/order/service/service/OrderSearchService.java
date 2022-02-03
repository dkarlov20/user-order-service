package com.griddynamics.gridu.user.order.service.service;

import com.griddynamics.gridu.user.order.service.configuration.OrderSearchServiceProperties;
import com.griddynamics.gridu.user.order.service.domain.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

import static com.griddynamics.gridu.user.order.service.util.ContextualLogging.logOnError;
import static com.griddynamics.gridu.user.order.service.util.ContextualLogging.logOnNext;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSearchService {
    private static final String PHONE_NUMBER_PARAM = "phoneNumber";

    private final OrderSearchServiceProperties orderSearchServiceProperties;
    private final WebClient webClient;

    public Flux<OrderDto> getUserOrdersByPhoneNumber(String phoneNumber) {
        String url = UriComponentsBuilder.fromHttpUrl(orderSearchServiceProperties.getHost())
                .port(orderSearchServiceProperties.getPort())
                .path(orderSearchServiceProperties.getOrdersByPhoneEndpoint())
                .queryParam(PHONE_NUMBER_PARAM, phoneNumber)
                .toUriString();

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(OrderDto.class)
                .doOnEach(logOnNext(orders -> log.info("Received Order for Phone Number: {} is: {}",
                        phoneNumber, orders)))
                .doOnEach(logOnError(throwable -> log.error("Error occurred during retrieving Orders, Phone Number: {}. Error: {}",
                        phoneNumber, throwable.getMessage())))
                .onErrorResume(throwable -> Flux.empty());
    }
}
