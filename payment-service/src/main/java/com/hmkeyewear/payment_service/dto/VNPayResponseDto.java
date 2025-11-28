package com.hmkeyewear.payment_service.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VNPayResponseDto {
    public String code;
    public String message;
    public String paymentUrl;
}
