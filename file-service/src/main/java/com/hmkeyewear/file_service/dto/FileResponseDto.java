package com.hmkeyewear.file_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileResponseDto {
    private String url;
    private String fileName;
    private long size;
    private Instant createdAt;
    private String createdBy;
    private String folder;
}
