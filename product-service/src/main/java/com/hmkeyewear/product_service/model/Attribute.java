package com.hmkeyewear.product_service.model;

import com.google.cloud.Timestamp;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Attribute {
    private String attributeId;
    private String key;
    private String label;
    private List<String> categoryIds;
    private List<AttributeOption> options;
    private Timestamp createdAt;
    private String createdBy;
}
