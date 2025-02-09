package dev.rocksdb.explorer.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class RocksDBServiceTest {

    @TempDir
    Path tempDir;

    private final RocksDBService service = new RocksDBService();

    @Test
    void shouldProcessValidDatabase() throws IOException {
        // Given
        Path testDbPath = tempDir.resolve("test-rocksdb-small.zip");
        Files.copy(
            getClass().getResourceAsStream("/test-dbs/test-rocksdb-small.zip"),
            testDbPath
        );
        MultipartFile file = new MockMultipartFile(
            "file",
            "test-rocksdb-small.zip",
            "application/zip",
            Files.readAllBytes(testDbPath)
        );

        // When
        Map<String, String> result = service.processDatabase(file);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals("John Doe", result.get("user:000001"));
        assertTrue(result.containsKey("config:theme"));
    }

    @Test
    void shouldRejectNonZipFile() {
        // Given
        MultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "not a zip file".getBytes()
        );

        // When/Then
        assertThrows(RuntimeException.class, () -> service.processDatabase(file));
    }

    @Test
    void shouldHandleEmptyZipFile() {
        // Given
        MultipartFile file = new MockMultipartFile(
            "file",
            "empty.zip",
            "application/zip",
            new byte[0]
        );

        // When/Then
        assertThrows(RuntimeException.class, () -> service.processDatabase(file));
    }
} 