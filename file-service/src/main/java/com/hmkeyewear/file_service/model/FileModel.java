package com.hmkeyewear.file_service.model;

import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
public class FileModel {
    private String fileName;
    private String path;
    private String url;
    private long size;
    private Instant createdAt;
    private String createdBy;
    private String folder;
}
