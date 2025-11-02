package com.hmkeyewear.product_service.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductInforResponseDto {
    private String productId;
    private String productName;
    private String brandId;
    private String categoryId;
    private String thumbnail;
    private double sellingPrice;
    private String status;
}
