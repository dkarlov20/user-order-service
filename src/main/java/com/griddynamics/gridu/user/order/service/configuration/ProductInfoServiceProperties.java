package com.griddynamics.gridu.user.order.service.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "user-order.product-info-service")
public class ProductInfoServiceProperties {
    private String host;
    private int port;
    private String productsByCodeEndpoint;
    private int requestTimeoutInSeconds;
}
