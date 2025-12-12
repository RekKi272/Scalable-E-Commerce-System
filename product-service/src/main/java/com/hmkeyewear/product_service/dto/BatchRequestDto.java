package com.hmkeyewear.product_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class BatchRequestDto {

    @NotEmpty
    @Valid
    @Getter
    @Setter
    private List<ItemRequestDto> items;

}
