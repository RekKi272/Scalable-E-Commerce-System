package com.hmkeyewear.product_service.mapper;

import com.hmkeyewear.product_service.dto.CategoryRequestDto;
import com.hmkeyewear.product_service.dto.CategoryResponseDto;
import com.hmkeyewear.product_service.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "categoryId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Category toCategory(CategoryRequestDto dto);

    CategoryResponseDto toCategoryResponseDto(Category entity);
}
