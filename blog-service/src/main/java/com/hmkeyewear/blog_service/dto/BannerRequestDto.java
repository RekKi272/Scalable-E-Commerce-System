package com.hmkeyewear.blog_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BannerRequestDto {
    private String title;
    private String thumbnail;
    private String status; // ACTIVE | INACTIVE
}
