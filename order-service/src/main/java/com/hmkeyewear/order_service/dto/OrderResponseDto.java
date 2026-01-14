package com.hmkeyewear.order_service.dto;

import com.hmkeyewear.order_service.model.OrderDetail;
import com.hmkeyewear.order_service.model.DiscountDetail;
import com.hmkeyewear.order_service.model.ShipInfo;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDto {
    private String orderId;
    private String userId;
    private String email;
    private String fullname;
    private String phone;

    private String paymentMethod;

    private double priceTemp;
    private double priceDecreased;
    private double summary;

    private String status;

    private DiscountDetail discount;
    private List<OrderDetail> details;
    private ShipInfo ship;

    private String note;

    private Timestamp createdAt;
    private String createdBy;
    private Timestamp updatedAt;
    private String updatedBy;
}
