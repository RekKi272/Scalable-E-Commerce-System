package com.hmkeyewear.common_dto.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiscountDto {
    private String discountId;
    private String valueType;
    private Long valueDiscount;
}
