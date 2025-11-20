package com.hmkeyewear.file_service.controller;

import com.hmkeyewear.file_service.dto.FileResponseDto;
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

    @PostMapping("/upload")
    public ResponseEntity<FileResponseDto> uploadSingle(
            @RequestPart("file") MultipartFile file,
            @RequestParam("bucket") String bucket,
            @RequestParam(value = "folder", required = false) String folder,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "userId", required = false) String userId
    ) throws Exception {
        return ResponseEntity.ok(fileService.uploadFile(file, bucket, folder, name, userId));
    }

    @PostMapping("/upload/multiple")
    public ResponseEntity<List<FileResponseDto>> uploadMultiple(
            @RequestPart("files") MultipartFile[] files,
            @RequestParam("bucket") String bucket,
            @RequestParam(value = "folder", required = false) String folder,
            @RequestParam(value = "nameList", required = false) String nameList,
            @RequestParam(value = "userId", required = false) String userId
    ) throws Exception {
        return ResponseEntity.ok(fileService.uploadMultipleFiles(files, bucket, folder, nameList, userId));
    }

    @DeleteMapping
    public ResponseEntity<String> deleteByUrl(@RequestParam("url") String url) throws Exception {
        return ResponseEntity.ok(fileService.deleteByUrl(url));
    }

    @GetMapping
    public ResponseEntity<FileResponseDto> getByUrl(@RequestParam("url") String url) throws Exception {
        return ResponseEntity.ok(fileService.getByUrl(url));
    }

    @GetMapping("/folder/{folderName}")
    public ResponseEntity<List<FileResponseDto>> listFolder(
            @RequestParam("bucket") String bucket,
            @PathVariable String folderName
    ) throws Exception {
        return ResponseEntity.ok(fileService.listFolder(bucket, folderName));
    }
}
