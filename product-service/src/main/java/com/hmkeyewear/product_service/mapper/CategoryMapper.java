package com.hmkeyewear.product_service.mapper;

import com.hmkeyewear.product_service.dto.CategoryRequestDto;
import com.hmkeyewear.product_service.dto.CategoryResponseDto;
import com.hmkeyewear.product_service.model.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category toCategory(CategoryRequestDto dto);
    CategoryResponseDto toCategoryResponseDto(Category entity);
}
