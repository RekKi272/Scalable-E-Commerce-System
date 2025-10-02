package com.hmkeyewear.cart_service.dto;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartResponseDto {
    private String customerId;
    private List<CartItemDto> items;
    private double total;
}
