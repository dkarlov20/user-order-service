package com.griddynamics.gridu.user.order.service.service.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.griddynamics.gridu.user.order.service.configuration.ProductInfoServiceProperties;
import com.griddynamics.gridu.user.order.service.domain.dto.ProductDto;
import com.griddynamics.gridu.user.order.service.service.ProductInfoService;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.lang.Math.toIntExact;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@WireMockTest(httpPort = 8082)
@SpringBootTest
@ActiveProfiles("test")
public class ProductInfoServiceIntegrationTest {

    private static final EasyRandom EASY_RANDOM = new EasyRandom();
    private static final double DEFAULT_DOUBLE = 0.0;
    private static final int NUMBER_OF_PRODUCTS = 5;
    private static final String PRODUCT_CODE_PARAM = "productCode";
    private static final String PRODUCT_CODE = "123";
    private static final int RESPONSE_DELAY = 10;
    private static final ProductDto DEFAULT_PRODUCT = new ProductDto(EMPTY, EMPTY, EMPTY, DEFAULT_DOUBLE);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductInfoService productInfoService;

    @Autowired
    private ProductInfoServiceProperties productInfoServiceProperties;

    @Test
    public void getMostRelevantProductByProductCodeTest(WireMockRuntimeInfo wireMockRuntimeInfo) throws JsonProcessingException {
        List<ProductDto> expectedProducts = EASY_RANDOM.objects(ProductDto.class, NUMBER_OF_PRODUCTS)
                .collect(toList());
        ProductDto expectedRelevantProduct = expectedProducts.stream()
                .max(Comparator.comparing(ProductDto::getScore))
                .get();

        String productsJson = objectMapper.writeValueAsString(expectedProducts);

        wireMockRuntimeInfo.getWireMock()
                .register(get(urlPathEqualTo(productInfoServiceProperties.getProductsByCodeEndpoint()))
                        .withQueryParam(PRODUCT_CODE_PARAM, equalTo(PRODUCT_CODE))
                        .willReturn(aResponse()
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .withBody(productsJson)));

        Mono<ProductDto> actualProduct = productInfoService.getMostRelevantProductByProductCode(PRODUCT_CODE);

        StepVerifier.create(actualProduct)
                .expectNext(expectedRelevantProduct)
                .verifyComplete();
    }

    @Test
    public void getMostRelevantProductByProductCodeWithTimeoutErrorTest(WireMockRuntimeInfo wireMockRuntimeInfo) throws JsonProcessingException {
        List<ProductDto> expectedProducts = EASY_RANDOM.objects(ProductDto.class, NUMBER_OF_PRODUCTS)
                .collect(toList());

        String productsJson = objectMapper.writeValueAsString(expectedProducts);

        wireMockRuntimeInfo.getWireMock()
                .register(get(urlPathEqualTo(productInfoServiceProperties.getProductsByCodeEndpoint()))
                        .withQueryParam(PRODUCT_CODE_PARAM, equalTo(PRODUCT_CODE))
                        .willReturn(aResponse()
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .withBody(productsJson)
                                .withFixedDelay(toIntExact(Duration.ofSeconds(RESPONSE_DELAY).toMillis()))));

        Mono<ProductDto> actualProduct = productInfoService.getMostRelevantProductByProductCode(PRODUCT_CODE);

        StepVerifier.create(actualProduct)
                .expectNext(DEFAULT_PRODUCT)
                .verifyComplete();
    }

    @Test
    public void getMostRelevantProductByProductCodeWithServerErrorTest(WireMockRuntimeInfo wireMockRuntimeInfo) {
        wireMockRuntimeInfo.getWireMock()
                .register(get(urlPathEqualTo(productInfoServiceProperties.getProductsByCodeEndpoint()))
                        .withQueryParam(PRODUCT_CODE_PARAM, equalTo(PRODUCT_CODE))
                        .willReturn(serverError()));

        Mono<ProductDto> actualProduct = productInfoService.getMostRelevantProductByProductCode(PRODUCT_CODE);

        StepVerifier.create(actualProduct)
                .expectNext(DEFAULT_PRODUCT)
                .verifyComplete();
    }
}