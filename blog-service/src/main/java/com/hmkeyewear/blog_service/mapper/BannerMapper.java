package com.hmkeyewear.blog_service.mapper;

import com.hmkeyewear.blog_service.dto.BannerRequestDto;
import com.hmkeyewear.blog_service.dto.BannerResponseDto;
import com.hmkeyewear.blog_service.model.Banner;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;

@Mapper(componentModel = "spring")
public interface BannerMapper {

    @Mapping(source = "title", target = "title")
    @Mapping(source = "thumbnail", target = "thumbnail")
    @Mapping(source = "status", target = "status")
    Banner toBanner(BannerRequestDto dto);

    @Mapping(source = "bannerId", target = "bannerId")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "thumbnail", target = "thumbnail")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "createdBy", target = "createdBy")
    @Mapping(source = "updatedAt", target = "updatedAt")
    @Mapping(source = "updatedBy", target = "updatedBy")
    BannerResponseDto toBannerResponseDto(Banner banner);

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
