package com.hmkeyewear.product_service.dto;

import lombok.Data;

@Data
public class OrderDetailRequestDto {
    private String productId;
    private String variantId;
    private String productName;
    private String unitPrice;
    private int quantity;
}
