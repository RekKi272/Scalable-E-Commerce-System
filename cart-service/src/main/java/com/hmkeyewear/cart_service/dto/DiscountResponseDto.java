package com.hmkeyewear.cart_service.dto;

import lombok.Data;

@Data
public class DiscountResponseDto {
    private String discountId;
    private String valueType; // (percentage/ fixed)
    private Long valueDiscount; // (10%, 50.000VND)
    private Long minPriceRequired; //
    private Long maxPriceDiscount; // optional

    private String description;
}
