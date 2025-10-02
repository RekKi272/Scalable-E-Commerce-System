package com.hmkeyewear.product_service.model;

import lombok.Getter;
import lombok.Setter;

import com.google.cloud.Timestamp;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Product {
    private String productId;
    private String brandId;
    private String categoryId;
    private String productName;
    private String description;
    private String status;
    private String thumbnail;
    private double importPrice;
    private double sellingPrice;

    // attributes dạng key-value
    private Map<String, Object> attributes;

    // danh sách variant
    private List<Variant> variants;

    private Timestamp createdAt;
    private String createdBy;
    private Timestamp updatedAt;
    private String updatedBy;
}
