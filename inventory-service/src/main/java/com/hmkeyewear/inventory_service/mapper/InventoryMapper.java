package com.hmkeyewear.inventory_service.mapper;

import com.hmkeyewear.inventory_service.dto.InventoryRequestDto;
import com.hmkeyewear.inventory_service.dto.InventoryResponseDto;
import com.hmkeyewear.inventory_service.model.Inventory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InventoryMapper {
    Inventory toInventory(InventoryRequestDto dto);
    InventoryResponseDto toInventoryResponseDto(Inventory inventory);
}
