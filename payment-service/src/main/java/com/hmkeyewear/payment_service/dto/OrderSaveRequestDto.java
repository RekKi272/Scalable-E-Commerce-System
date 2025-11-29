package com.hmkeyewear.payment_service.dto;

import com.hmkeyewear.payment_service.Model.OrderDetail;
import lombok.Data;

import java.util.List;

@Data
public class OrderSaveRequestDto {
    private String userId;
    private double summary;
    private List<CartItemDto> items; // GIỮ nguyên từ cart-service
    private String discountId;
}
