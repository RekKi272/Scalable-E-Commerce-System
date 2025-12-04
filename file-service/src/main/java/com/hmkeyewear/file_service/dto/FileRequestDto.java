package com.hmkeyewear.file_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileRequestDto {
    private FileDto file;
    private String fileName;
    private String bucket;
    private String folder;
    private String userId;
}
