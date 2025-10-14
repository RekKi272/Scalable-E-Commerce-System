package com.hmkeyewear.inventory_service.model;

import com.google.cloud.Timestamp;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Inventory {
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
