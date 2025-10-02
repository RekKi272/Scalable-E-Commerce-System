package com.hmkeyewear.product_service.model;

import lombok.Getter;
import lombok.Setter;

import com.google.cloud.Timestamp;

@Getter
@Setter
public class Brand {
    private String brandId;
    private String brandName;
    private Timestamp createdAt;
    private String createdBy;
}
