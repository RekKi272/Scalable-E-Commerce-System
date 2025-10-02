package com.hmkeyewear.order_service.model;

import lombok.Getter;
import lombok.Setter;

import com.google.cloud.Timestamp;
import java.util.List;

@Getter
@Setter
public class Order {
    private String orderId;
    private String customerId;
    private double summary;
    private String status;
    private double shipFee;
    private String discountId;
    private Timestamp createdAt;
    private String createdBy;
    private Timestamp updatedAt;
    private String updatedBy;
    private List<OrderDetail> details;
}
