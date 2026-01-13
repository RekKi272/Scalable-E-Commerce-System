package com.hmkeyewear.order_service.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShipInfo {
    private String addressProvince;
    private String addressWard;
    private String addressDetail;
    private double shippingFee = 30000;
}
