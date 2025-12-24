package com.hmkeyewear.order_service.dto;

import com.hmkeyewear.order_service.model.OrderDetail;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderSaveRequestDto {
    private String userId;
    private String email;
    private double summary;
    private List<OrderDetail> items; // GIỮ nguyên từ cart-service
    private String discountId;
}

