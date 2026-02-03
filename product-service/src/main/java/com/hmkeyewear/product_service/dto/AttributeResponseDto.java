package com.hmkeyewear.product_service.dto;

import com.google.cloud.Timestamp;
import lombok.Data;

import java.util.List;

@Data
public class AttributeResponseDto {
    private String attributeId;
    private String key;
    private String label;
    private List<String> categoryIds;
    private List<AttributeOptionDto> options;
    private Timestamp createdAt;
    private String createdBy;
}
