package com.hmkeyewear.order_service.model;

import lombok.Getter;
import lombok.Setter;

import com.google.cloud.Timestamp;
import java.util.List;

@Getter
@Setter
public class Order {
    private String orderId;

    private String userId;
    private String email;
    private String fullname;

    private String paymentMethod; // CASH, COD, BANK_TRANSFER

    private double priceTemp;
    private double priceDecreased;
    private double summary;

    private String status;

    private List<DiscountDetail> discount;
    private List<OrderDetail> details;
    private List<ShipInfo> ship;

    private String note;

    private Timestamp createdAt;
    private String createdBy;
    private Timestamp updatedAt;
    private String updatedBy;
}
