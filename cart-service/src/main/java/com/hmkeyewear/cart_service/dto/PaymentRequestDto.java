package com.hmkeyewear.cart_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequestDto {
    private String userId;
    private List<CartItemDto> items;
    private double total;
    private String discountId; // Optional
    private String bankCode; // Optional
    private String returnUrl;
    private String ipAddress;
}
