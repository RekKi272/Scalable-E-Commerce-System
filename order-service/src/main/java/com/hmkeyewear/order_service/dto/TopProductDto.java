package com.hmkeyewear.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopProductDto {
    private String productId;
    private String productName;
    private int totalQuantity;
}
