package com.hmkeyewear.file_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmkeyewear.file_service.dto.FileResponseDto;
import com.hmkeyewear.file_service.messaging.FileEventProducer;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FileService {

    private final FileEventProducer eventProducer;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private String supabaseUrl;
    private String serviceRoleKey;
    private String anonKey;

    public FileService(FileEventProducer eventProducer) {
        this.eventProducer = eventProducer;
        loadSecrets();
    }

    private void loadSecrets() {
        String path = System.getenv("SUPABASE_SECRETS_PATH");
        if (path == null) {
            throw new IllegalStateException("SUPABASE_SECRETS_PATH env variable is not set");
        }
        try (InputStream is = new FileInputStream(path)) {
            JsonNode node = objectMapper.readTree(is);
            this.supabaseUrl = node.get("supabaseUrl").asText();
            this.serviceRoleKey = node.get("serviceRoleKey").asText();
            this.anonKey = node.has("anonKey") ? node.get("anonKey").asText() : null;

            System.out.println("Supabase URL: " + supabaseUrl);
            System.out.println("Using key: " + (anonKey != null ? anonKey : serviceRoleKey));

        } catch (Exception e) {
            throw new IllegalStateException("Cannot load supabase secrets from " + path + " - " + e.getMessage(), e);
        }
    }

    private String encodePath(String path) {
        return Arrays.stream(path.split("/"))
                .map(segment -> {
                    try {
                        return URLEncoder.encode(segment, "UTF-8");
                    } catch (Exception e) {
                        return segment;
                    }
                })
                .collect(Collectors.joining("/"));
    }

    private String buildPublicUrl(String bucket, String objectPath) {
        return String.format("%s/storage/v1/object/public/%s/%s", supabaseUrl, bucket, encodePath(objectPath));
    }

    public FileResponseDto uploadFile(
            MultipartFile file,
            String bucket,
            String folder,
            String userId) throws Exception {

        if (folder == null || folder.isBlank() || folder.equals(bucket)) {
            folder = "";
        }

        String originalName = file.getOriginalFilename();
        String extension = "";

        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }

        String fileName = bucket + "_" + System.currentTimeMillis() + extension;
        String objectPath = (folder.isBlank() ? "" : folder + "/") + fileName;

        byte[] fileBytes = file.getBytes();
        String contentType = Optional.ofNullable(file.getContentType())
                .orElse("application/octet-stream");

        String url = String.format(
                "%s/storage/v1/object/%s/%s",
                supabaseUrl,
                bucket,
                encodePath(objectPath));

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + serviceRoleKey)
                .header("apikey", serviceRoleKey)
                .header("Content-Type", contentType)
                .PUT(HttpRequest.BodyPublishers.ofByteArray(fileBytes))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            String publicUrl = buildPublicUrl(bucket, objectPath);

            FileResponseDto dto = new FileResponseDto(
                    publicUrl,
                    fileName,
                    fileBytes.length,
                    Instant.now(),
                    userId,
                    folder);

            eventProducer.sendMessage(Map.of(
                    "action", "UPLOAD",
                    "url", publicUrl,
                    "fileName", fileName));

            return dto;
        }

        throw new RuntimeException(
                "Upload failed: HTTP " + response.statusCode() + " - " + response.body());
    }

    public List<FileResponseDto> uploadMultipleFiles(
            List<MultipartFile> files,
            String bucket,
            String folder,
            String userId) throws Exception {
        List<FileResponseDto> result = new ArrayList<>();
        for (MultipartFile file : files) {
            result.add(uploadFile(file, bucket, folder, userId));
        }
        return result;
    }

    public String deleteByUrl(String url) throws Exception {
        if (!url.contains("/storage/v1/object/public/")) {
            throw new IllegalArgumentException("Unsupported URL format: " + url);
        }

        String after = url.substring(
                url.indexOf("/storage/v1/object/public/") + "/storage/v1/object/public/".length());

        String[] parts = after.split("/", 2);
        String bucket = parts[0];

        // ✅ decode về path gốc
        String objectPath = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);

        String endpoint = String.format(
                "%s/storage/v1/object/%s/%s",
                supabaseUrl,
                bucket,
                objectPath);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Authorization", "Bearer " + serviceRoleKey)
                .header("apikey", serviceRoleKey)
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            eventProducer.sendMessage(Map.of("action", "DELETE", "url", url));
            return "Deleted: " + url;
        }

        throw new RuntimeException(
                "Delete failed: HTTP " + response.statusCode() + " - " + response.body());
    }

    public FileResponseDto getByUrl(String url) {
        if (!url.contains("/storage/v1/object/public/"))
            throw new IllegalArgumentException("Unsupported URL: " + url);

        String after = url.substring(url.indexOf("/storage/v1/object/public/") + 29);
        String[] parts = after.split("/", 2);

        String objectPath = parts[1];
        String fileName = Paths.get(objectPath).getFileName().toString();
        String folder = objectPath.contains("/") ? objectPath.substring(0, objectPath.lastIndexOf("/")) : "";

        return new FileResponseDto(url, fileName, -1L, Instant.now(), null, folder);
    }
}
