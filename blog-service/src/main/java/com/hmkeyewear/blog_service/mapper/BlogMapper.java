package com.hmkeyewear.blog_service.mapper;

import com.hmkeyewear.blog_service.dto.BlogRequestDto;
import com.hmkeyewear.blog_service.dto.BlogResponseDto;
import com.hmkeyewear.blog_service.model.Blog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;

@Mapper(componentModel = "spring")
public interface BlogMapper {

    // BlogRequestDto -> Blog
    @Mapping(source = "title", target = "title")
    @Mapping(source = "content", target = "content")
    @Mapping(source = "thumbnail", target = "thumbnail")
    @Mapping(source = "status", target = "status")
    Blog toBlog(BlogRequestDto blogRequestDto);

    // Blog -> BlogResponseDto
    @Mapping(source = "blogId", target = "blogId")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "content", target = "content")
    @Mapping(source = "thumbnail", target = "thumbnail")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "createdBy", target = "createdBy")
    BlogResponseDto toBlogResponseDto(Blog blog);

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
