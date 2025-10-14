package com.hmkeyewear.store_service.model;

import lombok.Getter;
import lombok.Setter;
import com.google.cloud.Timestamp;

@Getter
@Setter
public class Store {
    private String storeId;
    private String storeName;
    private String address;
    private String province;
    private String status;

    private Timestamp createdAt;
    private String createdBy;
}
