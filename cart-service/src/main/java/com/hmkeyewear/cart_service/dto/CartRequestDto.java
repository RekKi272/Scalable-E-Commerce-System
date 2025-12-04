package com.hmkeyewear.cart_service.dto;

import com.hmkeyewear.cart_service.model.CartItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartRequestDto {
    private String userId;
    private List<CartItem> items;
}
