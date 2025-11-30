package com.hmkeyewear.common_dto.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class VNPayResponseDto {
    private String code;
    private String message;
    private String paymentUrl;
}
