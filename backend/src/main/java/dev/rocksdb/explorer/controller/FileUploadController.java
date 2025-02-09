package dev.rocksdb.explorer.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import dev.rocksdb.explorer.service.RocksDBService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileUploadController {
    private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);
    private final RocksDBService rocksDBService;

    @PostMapping
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("Received file upload request: {}", file.getOriginalFilename());
        
        if (file.isEmpty() || !file.getOriginalFilename().endsWith(".zip")) {
            log.warn("Invalid file upload attempt: {}", file.getOriginalFilename());
            return ResponseEntity.badRequest().body("Please upload a zip file");
        }

        try {
            Map<String, String> keyValues = rocksDBService.processDatabase(file);
            log.info("Successfully processed database with {} entries", keyValues.size());
            return ResponseEntity.ok(keyValues);
        } catch (Exception e) {
            log.error("Failed to process database", e);
            return ResponseEntity.internalServerError().body("Failed to process database: " + e.getMessage());
        }
    }
} 