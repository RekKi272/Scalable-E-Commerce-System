package com.hmkeyewear.cart_service.dto;

import com.google.cloud.Timestamp;
import lombok.Data;

@Data
public class DiscountRequestDto {
    private String discountId; //
    private String valueType; // (percentage/ fixed)
    private Long valueDiscount; // (10%, 50.000VND)
    private Long minPriceRequired; //
    private Long maxPriceDiscount; // optional

    private Timestamp startDate;
    private Timestamp endDate;

    private int maxQuantity;
    private int usedQuantity;

    private String description; // Introduce the discount
}
