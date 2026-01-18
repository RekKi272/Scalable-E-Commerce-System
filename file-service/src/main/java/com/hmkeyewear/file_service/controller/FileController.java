package com.hmkeyewear.file_service.controller;

import com.hmkeyewear.file_service.dto.FileResponseDto;
import com.hmkeyewear.file_service.dto.FileRequestDto;
import com.hmkeyewear.file_service.service.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/file")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<FileResponseDto> uploadSingle(
            @RequestPart("file") MultipartFile file,
            @RequestParam String bucket,
            @RequestParam(required = false) String folder,
            @RequestParam(required = false) String userId) throws Exception {
        return ResponseEntity.ok(
                fileService.uploadFile(file, bucket, folder, userId));
    }

    @PostMapping(value = "/upload/multiple", consumes = "multipart/form-data")
    public ResponseEntity<List<FileResponseDto>> uploadMultiple(
            @RequestPart("files") List<MultipartFile> files,
            @RequestParam String bucket,
            @RequestParam(required = false) String folder,
            @RequestParam(required = false) String userId) throws Exception {
        return ResponseEntity.ok(
                fileService.uploadMultipleFiles(files, bucket, folder, userId));
    }

    @DeleteMapping
    public ResponseEntity<String> deleteByUrl(@RequestParam("url") String url) throws Exception {
        return ResponseEntity.ok(fileService.deleteByUrl(url));
    }

    @GetMapping
    public ResponseEntity<FileResponseDto> getByUrl(@RequestParam("url") String url) {
        return ResponseEntity.ok(fileService.getByUrl(url));
    }
}
