package com.hmkeyewear.cart_service.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItem {
    private String productId;
    private String variantId;
    private String productName;
    private double unitPrice;
    private int quantity;
    private String thumbnail;
}
