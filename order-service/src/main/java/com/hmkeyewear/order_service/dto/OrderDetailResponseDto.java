package com.hmkeyewear.order_service.dto;

import lombok.Data;

@Data
public class OrderDetailResponseDto {
    private String productId;
    private String variantId;
    private String productName;
    private int quantity;
    private double unitPrice;
    private double totalPrice;
}
