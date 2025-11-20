package com.hmkeyewear.file_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmkeyewear.file_service.dto.FileResponseDto;
import com.hmkeyewear.file_service.messaging.FileEventProducer;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
        } catch (Exception e) {
            throw new IllegalStateException("Cannot load supabase secrets from " + path + " - " + e.getMessage(), e);
        }
    }

    private String encodePath(String path) {
        return Arrays.stream(path.split("/"))
                .map(segment -> {
                    try { return URLEncoder.encode(segment, "UTF-8"); }
                    catch (Exception e) { return segment; }
                })
                .collect(Collectors.joining("/"));
    }

    private String buildPublicUrl(String bucket, String objectPath) {
        return String.format("%s/storage/v1/object/public/%s/%s", supabaseUrl, bucket, encodePath(objectPath));
    }

    /**
     * Upload single file. Nếu cùng tên file đã tồn tại, Supabase sẽ tự động ghi đè.
     */
    public FileResponseDto uploadFile(MultipartFile file, String bucket, String folder, String name, String userId) throws Exception {
        String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
        String fileNameToUse = (name == null || name.isBlank()) ? originalFilename : name;

        // Nếu folder trùng với bucket, bỏ folder để tránh 2 cấp
        String objectPath;
        if (folder == null || folder.isBlank() || folder.equals(bucket)) {
            objectPath = fileNameToUse;
            folder = ""; // để metadata trả về trống
        } else {
            objectPath = folder + "/" + fileNameToUse;
        }

        String url = String.format("%s/storage/v1/object/%s/%s", supabaseUrl, bucket, encodePath(objectPath));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + serviceRoleKey)
                .header("apikey", serviceRoleKey)
                .header("Content-Type", Optional.ofNullable(file.getContentType()).orElse("application/octet-stream"))
                .PUT(HttpRequest.BodyPublishers.ofByteArray(file.getBytes()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            String publicUrl = buildPublicUrl(bucket, objectPath);
            FileResponseDto dto = new FileResponseDto(publicUrl, fileNameToUse, file.getSize(), Instant.now(), userId, folder);

            eventProducer.sendMessage(Map.of(
                    "action", "UPLOAD",
                    "url", publicUrl,
                    "fileName", fileNameToUse,
                    "userId", userId
            ));
            return dto;
        } else {
            throw new RuntimeException("Upload failed: HTTP " + response.statusCode() + " - " + response.body());
        }
    }

    public List<FileResponseDto> uploadMultipleFiles(MultipartFile[] files, String bucket, String folder, String nameList, String userId) throws Exception {
        List<String> nameTokens = new ArrayList<>();
        if (nameList != null && !nameList.isBlank()) {
            nameTokens = Arrays.stream(nameList.split(","))
                    .map(String::trim).collect(Collectors.toList());
        }

        List<FileResponseDto> result = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            String chosenName = (i < nameTokens.size() && !nameTokens.get(i).isEmpty()) ? nameTokens.get(i) : file.getOriginalFilename();
            result.add(uploadFile(file, bucket, folder, chosenName, userId));
        }
        return result;
    }

    public String deleteByUrl(String url) throws Exception {
        if (!url.contains("/storage/v1/object/public/")) {
            throw new IllegalArgumentException("Unsupported URL format: " + url);
        }
        String after = url.substring(url.indexOf("/storage/v1/object/public/") + "/storage/v1/object/public/".length());
        String[] parts = after.split("/", 2);
        if (parts.length < 2) throw new IllegalArgumentException("Cannot parse object path from url: " + url);
        String bucket = parts[0];
        String objectPath = parts[1];

        String endpoint = String.format("%s/storage/v1/object/%s/%s", supabaseUrl, bucket, encodePath(objectPath));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Authorization", "Bearer " + serviceRoleKey)
                .header("apikey", serviceRoleKey)
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 204 || (response.statusCode() >= 200 && response.statusCode() < 300)) {
            eventProducer.sendMessage(Map.of("action", "DELETE", "url", url));
            return "Deleted: " + url;
        } else {
            throw new RuntimeException("Delete failed: HTTP " + response.statusCode() + " - " + response.body());
        }
    }

    public FileResponseDto getByUrl(String url) {
        if (!url.contains("/storage/v1/object/public/")) throw new IllegalArgumentException("Unsupported URL format: " + url);
        String after = url.substring(url.indexOf("/storage/v1/object/public/") + "/storage/v1/object/public/".length());
        String[] parts = after.split("/", 2);
        String bucket = parts[0];
        String objectPath = parts[1];
        String fileName = Paths.get(objectPath).getFileName().toString();
        String folder = objectPath.contains("/") ? objectPath.substring(0, objectPath.lastIndexOf("/")) : "";
        return new FileResponseDto(url, fileName, -1L, Instant.now(), null, folder);
    }

    public List<FileResponseDto> listFolder(String bucket, String folder) throws Exception {
        String endpoint = String.format("%s/storage/v1/object/list/%s?prefix=%s", supabaseUrl, bucket, URLEncoder.encode(folder, "UTF-8"));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Authorization", "Bearer " + serviceRoleKey)
                .header("apikey", serviceRoleKey)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            JsonNode node = objectMapper.readTree(response.body());
            List<FileResponseDto> list = new ArrayList<>();
            JsonNode dataArray = node.isArray() ? node : node.get("data");
            if (dataArray != null && dataArray.isArray()) {
                for (JsonNode item : dataArray) {
                    String name = item.has("name") ? item.get("name").asText() : null;
                    long size = item.has("size") ? item.get("size").asLong() : -1L;
                    String publicUrl = buildPublicUrl(bucket, name);
                    String fileName = Paths.get(name).getFileName().toString();
                    String folderName = name.contains("/") ? name.substring(0, name.lastIndexOf("/")) : "";
                    list.add(new FileResponseDto(publicUrl, fileName, size, Instant.now(), null, folderName));
                }
            }
            return list;
        } else {
            throw new RuntimeException("List folder failed: HTTP " + response.statusCode() + " - " + response.body());
        }
    }
}
