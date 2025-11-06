package com.hmkeyewear.product_service.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import com.google.cloud.Timestamp;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Product {
    @JsonProperty("objectID")
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

    // Danh sách ảnh của product
    private List<Image> images;

    private Timestamp createdAt;
    private String createdBy;
    private Timestamp updatedAt;
    private String updatedBy;
}
