package com.hmkeyewear.inventory_service.dto;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryResponseDto {
    private String inventoryId;
    private String productId;
    private String variantId;
    private Long quantityImport;
    private Long quantitySell;
    private Timestamp createdAt;
    private String createdBy;
    private Timestamp updatedAt;
    private String updatedBy;
}
