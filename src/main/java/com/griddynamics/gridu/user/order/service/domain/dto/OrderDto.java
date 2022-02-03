package com.griddynamics.gridu.user.order.service.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class OrderDto {
    private final String phoneNumber;
    private final String orderNumber;
    private final String productCode;

    public OrderDto(@JsonProperty("phoneNumber") String phoneNumber,
                    @JsonProperty("orderNumber") String orderNumber,
                    @JsonProperty("productCode") String productCode) {
        this.phoneNumber = phoneNumber;
        this.orderNumber = orderNumber;
        this.productCode = productCode;
    }
}
