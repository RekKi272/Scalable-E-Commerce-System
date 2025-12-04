package com.hmkeyewear.cart_service.model;

import com.google.cloud.Timestamp;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Discount {
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

    private Timestamp createdAt;
    private String createdBy;
    private Timestamp updatedAt;
    private String updatedBy;

}
