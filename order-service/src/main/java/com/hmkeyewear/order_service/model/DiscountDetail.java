package com.hmkeyewear.order_service.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiscountDetail {
    private String discountId;
    private String valueType; // percentage | fixed
    private Long valueDiscount;
}
