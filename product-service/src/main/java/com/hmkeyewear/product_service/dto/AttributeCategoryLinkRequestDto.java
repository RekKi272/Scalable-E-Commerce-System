package com.hmkeyewear.product_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttributeCategoryLinkRequestDto {
    private String attributeId;
    private String categoryId;
    private boolean attach; // true = add, false = remove
}
