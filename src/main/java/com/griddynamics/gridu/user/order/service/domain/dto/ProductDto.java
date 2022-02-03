package com.griddynamics.gridu.user.order.service.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class ProductDto {
    private final String productId;
    private final String productCode;
    private final String productName;
    private final Double score;

    public ProductDto(@JsonProperty("productId") String productId,
                      @JsonProperty("productCode") String productCode,
                      @JsonProperty("productName") String productName,
                      @JsonProperty("score") Double score) {
        this.productId = productId;
        this.productCode = productCode;
        this.productName = productName;
        this.score = score;
    }
}