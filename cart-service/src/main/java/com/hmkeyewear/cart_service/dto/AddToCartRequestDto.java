package com.hmkeyewear.cart_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddToCartRequestDto {
    private String productId;
    private String variantId;
    private String color;
    private String productName;
    private double unitPrice;
    private int quantity;
    private String thumbnail;
}
