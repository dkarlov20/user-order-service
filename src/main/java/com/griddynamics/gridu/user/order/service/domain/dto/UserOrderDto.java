package com.griddynamics.gridu.user.order.service.domain.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@Builder(toBuilder = true)
public class UserOrderDto {
    private final String orderNumber;
    private final String userName;
    private final String phoneNumber;
    private final String productCode;
    private final String productName;
    private final String productId;
}
