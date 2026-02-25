package com.hmkeyewear.product_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VariantListResponseDto {

    private String productId;
    private String variantId;
    private String productName;
    private Long stockQuantity;
    private double sellingPrice;
    private String color;
    private String thumbnail;
}
