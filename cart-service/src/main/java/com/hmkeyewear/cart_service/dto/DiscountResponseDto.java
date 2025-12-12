package com.hmkeyewear.cart_service.dto;

import lombok.Data;

@Data
public class DiscountResponseDto {
    private String discountId;
    private String valueType;
    private Long valueDiscount;
    private Long minPriceRequired;
    private Long maxPriceDiscount;

    private String startDate; // đổi thành String
    private String endDate; // đổi thành String

    private int maxQuantity;
    private int usedQuantity;
    private String description;
}
