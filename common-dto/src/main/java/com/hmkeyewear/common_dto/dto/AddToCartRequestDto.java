package com.hmkeyewear.common_dto.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class AddToCartRequestDto {
    private String productId;
    private String variantId;
    private String productName;
    private double unitPrice;
    private int quantity;
    private String thumbnail;
}