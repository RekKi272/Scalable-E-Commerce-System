package com.hmkeyewear.order_service.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderDetail {
    private String productId;
    private String productName;
    private int quantity;
    private double unitPrice;
    private double totalPrice;
}
