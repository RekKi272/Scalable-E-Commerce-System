package com.hmkeyewear.common_dto.dto;

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

    private List<CartItemDto> items;
    private ShipInfoDto ship;
    private DiscountDto discount;

    private String note;
}
