package com.hmkeyewear.product_service.model;

import lombok.Getter;
import lombok.Setter;

import com.google.cloud.Timestamp;

@Getter
@Setter
public class Category {
    private String categoryId;
    private String categoryName;
    private Timestamp createdAt;
    private String createdBy;
}
