package com.hmkeyewear.product_service.mapper;

import com.hmkeyewear.product_service.dto.ProductInforResponseDto;
import com.hmkeyewear.product_service.dto.ProductRequestDto;
import com.hmkeyewear.product_service.dto.ProductResponseDto;
import com.hmkeyewear.product_service.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    Product toProduct(ProductRequestDto productRequestDto);

    ProductResponseDto toProductResponseDto(Product product);

    ProductInforResponseDto toProductInforResponseDto(Product product);

    default Instant map(com.google.cloud.Timestamp timestamp) {
        return timestamp != null ? timestamp.toDate().toInstant() : null;
    }

    default com.google.cloud.Timestamp map(Instant instant) {
        return instant != null
                ? com.google.cloud.Timestamp.ofTimeSecondsAndNanos(
                instant.getEpochSecond(),
                instant.getNano())
                : null;
    }
}
