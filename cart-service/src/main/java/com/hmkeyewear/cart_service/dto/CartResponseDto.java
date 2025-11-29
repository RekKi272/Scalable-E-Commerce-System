package com.hmkeyewear.cart_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartResponseDto {
    private String userId;
    private List<CartItemDto> items;
    private String discountId;
    private double total;
}
