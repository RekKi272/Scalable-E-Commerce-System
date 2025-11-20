package com.hmkeyewear.file_service.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class FileMetadata {
    private String fileName;
    private String path; // e.g., folder/filename.ext
    private String url; // public url
    private long size;
    private Instant createdAt;
    private String createdBy;
    private String folder;
}
