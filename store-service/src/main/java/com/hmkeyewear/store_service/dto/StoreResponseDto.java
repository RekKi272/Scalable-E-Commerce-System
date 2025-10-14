package com.hmkeyewear.store_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.google.cloud.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreResponseDto {
    private String storeId;
    private String storeName;
    private String address;
    private String province;
    private String status;

    private Timestamp createdAt;
    private String createdBy;
}
