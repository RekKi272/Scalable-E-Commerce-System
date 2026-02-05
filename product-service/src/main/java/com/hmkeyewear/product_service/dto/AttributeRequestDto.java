package com.hmkeyewear.product_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AttributeRequestDto {

    @NotBlank
    private String key;

    @NotBlank
    private String label;

    @NotEmpty
    private List<String> categoryIds;

    private List<AttributeOptionDto> options;
}
