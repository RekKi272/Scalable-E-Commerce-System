package com.hmkeyewear.cart_service.model;

import com.google.cloud.Timestamp;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Cart {
    private String customerId;
    private List<CartItem> items = new ArrayList<CartItem>();
    private double total;
}
