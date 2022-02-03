package com.griddynamics.gridu.user.order.service.service;

import com.griddynamics.gridu.user.order.service.configuration.ProductInfoServiceProperties;
import com.griddynamics.gridu.user.order.service.domain.dto.ProductDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

import static com.griddynamics.gridu.user.order.service.util.ContextualLogging.logOnError;
import static com.griddynamics.gridu.user.order.service.util.ContextualLogging.logOnNext;
import static java.util.Collections.emptyList;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductInfoService {
    private static final String DEFAULT_STRING = "";
    private static final double DEFAULT_DOUBLE = 0.0;
    private static final String PRODUCT_CODE_PARAM = "productCode";

    private final ProductInfoServiceProperties productInfoServiceProperties;
    private final WebClient webClient;

    public Mono<ProductDto> getMostRelevantProductByProductCode(String productCode) {
        String url = UriComponentsBuilder.fromHttpUrl(productInfoServiceProperties.getHost())
                .port(productInfoServiceProperties.getPort())
                .path(productInfoServiceProperties.getProductsByCodeEndpoint())
                .queryParam(PRODUCT_CODE_PARAM, productCode)
                .toUriString();

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ProductDto>>() {})
                .timeout(Duration.ofSeconds(productInfoServiceProperties.getRequestTimeoutInSeconds()))
                .doOnEach(logOnNext(products -> log.info("Received Products for Product Code: {} are: {}",
                        productCode, products)))
                .doOnEach(logOnError(throwable -> log.error("Error occurred during retrieving Products, Product Code: {}. Error: {}",
                        productCode, throwable.getMessage())))
                .onErrorReturn(emptyList())
                .flatMapMany(Flux::fromIterable)
                .reduce((product1, product2) -> product1.getScore() >= product2.getScore() ? product1 : product2)
                .defaultIfEmpty(new ProductDto(DEFAULT_STRING, DEFAULT_STRING, DEFAULT_STRING, DEFAULT_DOUBLE));
    }
}
