package com.hmkeyewear.blog_service.model;

import com.google.cloud.Timestamp;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Banner {
    private String bannerId;
    private String title;
    private String imageBase64;
    private String status;
    private Timestamp createdAt;
    private String createdBy;
    private Timestamp updatedAt;
    private String updatedBy;
}
