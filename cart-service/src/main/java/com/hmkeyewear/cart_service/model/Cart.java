package com.hmkeyewear.cart_service.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Cart {
    private String userId;
    private List<CartItem> items = new ArrayList<CartItem>();
    private double total;
}
