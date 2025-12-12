package com.hmkeyewear.product_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemRequestDto {
    @NotBlank(message = "Product ID is required")
    private String productId;

    @NotBlank(message = "Variant ID is required")
    private String variantId;

    @NotNull(message = "Quantity is required")
    private Long quantity;
}