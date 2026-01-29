package com.hmkeyewear.common_dto.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipInfoDto {
    private String addressProvince;
    private String addressWard;
    private String addressDetail;
    private double shippingFee;
}
