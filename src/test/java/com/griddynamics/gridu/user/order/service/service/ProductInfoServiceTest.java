package com.griddynamics.gridu.user.order.service.service;

import com.griddynamics.gridu.user.order.service.configuration.ProductInfoServiceProperties;
import com.griddynamics.gridu.user.order.service.domain.dto.ProductDto;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductInfoServiceTest {

    private static final EasyRandom EASY_RANDOM = new EasyRandom();
    private static final double DEFAULT_DOUBLE = 0.0;
    private static final int NUMBER_OF_PRODUCTS = 5;
    private static final String PRODUCT_CODE_PARAM = "productCode";
    private static final String PRODUCT_CODE = "123";
    private static final ProductDto DEFAULT_PRODUCT = new ProductDto(EMPTY, EMPTY, EMPTY, DEFAULT_DOUBLE);
    private static final String HOST = "http://localhost";
    private static final int PORT = 8080;
    private static final String ORDER_PRODUCTS_ENDPOINT = "/productInfoService/product/names";
    private static final int TIMEOUT = 10;
    private static final String EXPECTED_URL = UriComponentsBuilder.fromHttpUrl(HOST)
            .port(PORT)
            .path(ORDER_PRODUCTS_ENDPOINT)
            .queryParam(PRODUCT_CODE_PARAM, PRODUCT_CODE)
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
    private ProductInfoServiceProperties productInfoServiceProperties;

    @InjectMocks
    private ProductInfoService productInfoService;

    @Test
    public void getMostRelevantProductByProductCodeTest() {
        List<ProductDto> expectedProducts = EASY_RANDOM.objects(ProductDto.class, NUMBER_OF_PRODUCTS)
                .collect(toList());
        ProductDto expectedRelevantProduct = expectedProducts.stream()
                .max(Comparator.comparing(ProductDto::getScore))
                .get();

        when(productInfoServiceProperties.getHost())
                .thenReturn(HOST);
        when(productInfoServiceProperties.getPort())
                .thenReturn(PORT);
        when(productInfoServiceProperties.getProductsByCodeEndpoint())
                .thenReturn(ORDER_PRODUCTS_ENDPOINT);
        when(productInfoServiceProperties.getRequestTimeoutInSeconds())
                .thenReturn(TIMEOUT);
        when(webClient.get())
                .thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(EXPECTED_URL))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve())
                .thenReturn(responseSpec);
        when(responseSpec.bodyToMono(new ParameterizedTypeReference<List<ProductDto>>() {}))
                .thenReturn(Mono.just(expectedProducts));

        Mono<ProductDto> actualProduct = productInfoService.getMostRelevantProductByProductCode(PRODUCT_CODE);

        StepVerifier.create(actualProduct)
                .expectNext(expectedRelevantProduct)
                .verifyComplete();

        verify(productInfoServiceProperties).getHost();
        verify(productInfoServiceProperties).getRequestTimeoutInSeconds();
        verify(productInfoServiceProperties).getPort();
        verify(productInfoServiceProperties).getProductsByCodeEndpoint();
        verify(webClient).get();
        verify(requestHeadersUriSpec).uri(EXPECTED_URL);
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(new ParameterizedTypeReference<List<ProductDto>>() {});

        verifyNoMoreInteractions(webClient, requestHeadersUriSpec, requestHeadersSpec, responseSpec);
    }

    @Test
    public void getMostRelevantProductByProductCodeWithServerErrorTest() {
        Throwable mockThrowable = mock(Throwable.class);

        when(productInfoServiceProperties.getHost())
                .thenReturn(HOST);
        when(productInfoServiceProperties.getPort())
                .thenReturn(PORT);
        when(productInfoServiceProperties.getProductsByCodeEndpoint())
                .thenReturn(ORDER_PRODUCTS_ENDPOINT);
        when(productInfoServiceProperties.getRequestTimeoutInSeconds())
                .thenReturn(TIMEOUT);
        when(webClient.get())
                .thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(EXPECTED_URL))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve())
                .thenReturn(responseSpec);
        when(responseSpec.bodyToMono(new ParameterizedTypeReference<List<ProductDto>>() {}))
                .thenReturn(Mono.error(mockThrowable));

        Mono<ProductDto> actualProduct = productInfoService.getMostRelevantProductByProductCode(PRODUCT_CODE);

        StepVerifier.create(actualProduct)
                .expectNext(DEFAULT_PRODUCT)
                .verifyComplete();

        verify(productInfoServiceProperties).getHost();
        verify(productInfoServiceProperties).getRequestTimeoutInSeconds();
        verify(productInfoServiceProperties).getPort();
        verify(productInfoServiceProperties).getProductsByCodeEndpoint();
        verify(webClient).get();
        verify(requestHeadersUriSpec).uri(EXPECTED_URL);
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(new ParameterizedTypeReference<List<ProductDto>>() {});

        verifyNoMoreInteractions(webClient, requestHeadersUriSpec, requestHeadersSpec, responseSpec);
    }
}