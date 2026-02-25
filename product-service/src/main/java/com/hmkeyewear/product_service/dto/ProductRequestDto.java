package com.hmkeyewear.product_service.dto;

import com.hmkeyewear.product_service.model.Variant;
import com.hmkeyewear.product_service.model.Image;
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

    @NotBlank
    private String productName;

    @NotBlank
    private String brandId;

    @NotBlank
    private String categoryId;

    private String description;
    private String status;
    private String thumbnail;

    @Min(0)
    private double importPrice;

    @Min(0)
    private double sellingPrice;

    private Map<String, Object> attributes;

    private List<Image> images;
}
