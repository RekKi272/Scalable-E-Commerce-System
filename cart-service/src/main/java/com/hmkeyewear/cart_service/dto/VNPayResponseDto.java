package com.hmkeyewear.cart_service.dto;

import lombok.Data;

@Data
public class VNPayResponseDto {
    private String code;
    private String message;
    private String paymentUrl;
}
