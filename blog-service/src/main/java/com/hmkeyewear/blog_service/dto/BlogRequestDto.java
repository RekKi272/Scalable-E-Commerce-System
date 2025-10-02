package com.hmkeyewear.blog_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlogRequestDto {
    private String title;
    private String content;
    private String thumbnail;
    private String status;
}
