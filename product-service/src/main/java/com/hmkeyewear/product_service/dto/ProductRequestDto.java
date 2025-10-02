package com.hmkeyewear.product_service.dto;

import com.hmkeyewear.product_service.model.Variant;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequestDto {
    @NotBlank(message = "Product name is required")
    private String productName;

    @NotBlank(message = "Brand ID is required")
    private String brandId;

    @NotBlank(message = "Category ID is required")
    private String categoryId;

    private String description;
    private String status;
    private String thumbnail;

    @Min(value = 0, message = "Import price must be >= 0")
    private double importPrice;

    @Min(value = 0, message = "Selling price must be >= 0")
    private double sellingPrice;

    private Map<String, Object> attributes;
    private List<Variant> variants;

    private String createdBy;
    private String updatedBy;

}
