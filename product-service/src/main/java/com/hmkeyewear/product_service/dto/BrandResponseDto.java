package com.hmkeyewear.product_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.google.cloud.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BrandResponseDto {
    private String brandId;
    private String brandName;
    private Timestamp createdAt;
    private String createdBy;
}
