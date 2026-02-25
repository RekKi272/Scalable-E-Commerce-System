package com.hmkeyewear.common_dto.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDto {
    private String productId;
    private String variantId;
    private String color;
    private String productName;
    private double unitPrice;
    private int quantity;
    private double totalPrice;
    private String thumbnail;
}
