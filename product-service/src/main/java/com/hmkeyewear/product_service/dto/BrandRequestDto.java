package com.hmkeyewear.product_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BrandRequestDto {
    @NotBlank(message = "Brand name is required")
    private String brandName;
    private String createdBy;
}
