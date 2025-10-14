package com.hmkeyewear.inventory_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class InventoryBatchRequestDto {

    @NotEmpty
    @Valid
    private List<InventoryRequestDto> items;

    public List<InventoryRequestDto> getItems() {
        return items;
    }

    public void setItems(List<InventoryRequestDto> items) {
        this.items = items;
    }
}
