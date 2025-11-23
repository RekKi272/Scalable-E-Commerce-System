package com.hmkeyewear.file_service.controller;

import com.hmkeyewear.file_service.dto.FileResponseDto;
import com.hmkeyewear.file_service.dto.FileRequestDto;
import com.hmkeyewear.file_service.service.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/file")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    // Upload single file
    @PostMapping("/upload")
    public ResponseEntity<FileResponseDto> uploadSingle(@RequestBody FileRequestDto request) throws Exception {
        return ResponseEntity.ok(fileService.uploadFile(request));
    }

    // Upload multiple files
    @PostMapping("/upload/multiple")
    public ResponseEntity<List<FileResponseDto>> uploadMultiple(@RequestBody List<FileRequestDto> requests) throws Exception {
        return ResponseEntity.ok(fileService.uploadMultipleFiles(requests));
    }

    // Delete file by public url
    @DeleteMapping
    public ResponseEntity<String> deleteByUrl(@RequestParam("url") String url) throws Exception {
        return ResponseEntity.ok(fileService.deleteByUrl(url));
    }

    // Get file info by public url
    @GetMapping
    public ResponseEntity<FileResponseDto> getByUrl(@RequestParam("url") String url) throws Exception {
        return ResponseEntity.ok(fileService.getByUrl(url));
    }
}
