package com.hmkeyewear.common_dto.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class OrderDetailRequestDto {
    private String productId;
    private String variantId;
    private String productName;
    private String unitPrice;
    private int quantity;
}
