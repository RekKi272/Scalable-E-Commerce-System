package com.hmkeyewear.common_dto.dto;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequestDto {
    private String userId;
    private String orderId;
    private List<CartItemDto> items;
    private double total;
    private String discountId;
    private String bankCode; // Optional
    private String returnUrl;
    private String ipAddress;
}