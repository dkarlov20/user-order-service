package com.griddynamics.gridu.user.order.service;

import com.griddynamics.gridu.user.order.service.configuration.OrderSearchServiceProperties;
import com.griddynamics.gridu.user.order.service.configuration.ProductInfoServiceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({OrderSearchServiceProperties.class, ProductInfoServiceProperties.class})
public class UserOrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserOrderServiceApplication.class, args);
    }

}
