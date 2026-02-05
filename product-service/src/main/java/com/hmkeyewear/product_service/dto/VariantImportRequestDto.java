package com.hmkeyewear.product_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VariantImportRequestDto {

    @NotBlank
    private String variantId;

    @NotNull
    private Long quantity;
}
