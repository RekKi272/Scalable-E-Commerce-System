package com.hmkeyewear.store_service.mapper;

import com.hmkeyewear.store_service.dto.StoreRequestDto;
import com.hmkeyewear.store_service.dto.StoreResponseDto;
import com.hmkeyewear.store_service.model.Store;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StoreMapper {
    Store toStore(StoreRequestDto dto);
    StoreResponseDto toStoreResponseDto(Store store);
}
