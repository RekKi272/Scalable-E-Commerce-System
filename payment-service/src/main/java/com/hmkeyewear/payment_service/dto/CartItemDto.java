package com.hmkeyewear.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDto {
    private String productId;
    private String variantId;
    private String productName;
    private double unitPrice;
    private int quantity;
    private String thumbnail;
}

