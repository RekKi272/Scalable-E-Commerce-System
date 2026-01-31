package com.hmkeyewear.common_dto.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDto {
    private String productId;
    private String variantId;
    private String productName;
    private int quantity;
    private double unitPrice;
    private double totalPrice;
}
