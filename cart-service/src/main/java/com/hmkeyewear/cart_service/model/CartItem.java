package com.hmkeyewear.cart_service.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {
    private String productId;
    private String variantId;
    private String color;
    private String productName;
    private double unitPrice;
    private int quantity;
    private String thumbnail;
}
