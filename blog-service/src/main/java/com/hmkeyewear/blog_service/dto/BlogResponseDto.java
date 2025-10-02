package com.hmkeyewear.blog_service.dto;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.A;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlogResponseDto {
    private String blogId;
    private String title;
    private String content;
    private String thumbnail;
    private String status;
    private Timestamp createdAt;
    private String createdBy;
}
