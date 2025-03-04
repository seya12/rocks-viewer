package dev.rocksdb.explorer.service;

import java.io.IOException;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import dev.rocksdb.explorer.util.TestUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class RocksDBServiceTest {

    @TempDir
    Path tempDir;

    private final RocksDBService service = new RocksDBService();

    @Test
    void shouldProcessValidDatabase() throws Exception {
        // Given
        Path testDbPath = TestUtils.createTestDatabase(tempDir);
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
        
        // Use a JSON parser to compare the values to handle whitespace differences
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualUser = mapper.readTree(result.get("user:000001"));
        assertEquals(1, actualUser.get("id").asInt());
        assertEquals("User 1", actualUser.get("name").asText());
        assertEquals("dark", actualUser.get("profile").get("preferences").get("theme").asText());

        JsonNode actualPrefs = mapper.readTree(result.get("user:000001.profile.preferences"));
        assertEquals("dark", actualPrefs.get("theme").asText());
        assertEquals("en-US", actualPrefs.get("language").asText());
        assertTrue(actualPrefs.get("notifications").asBoolean());
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

    @Test
    void shouldHandleCorruptedDatabase() throws IOException {
        // Given
        Path corruptedDbPath = tempDir.resolve("corrupted.zip");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(corruptedDbPath.toFile()))) {
            ZipEntry entry = new ZipEntry("corrupted/CURRENT");
            zos.putNextEntry(entry);
            zos.write("corrupted data".getBytes());
            zos.closeEntry();
        }

        MultipartFile file = new MockMultipartFile(
            "file",
            "corrupted.zip",
            "application/zip",
            Files.readAllBytes(corruptedDbPath)
        );

        // When/Then
        assertThrows(RuntimeException.class, () -> service.processDatabase(file));
    }

    @Test
    void shouldHandleMalformedZipFile() throws IOException {
        // Given
        Path malformedPath = tempDir.resolve("malformed.zip");
        Files.write(malformedPath, "not a valid zip file".getBytes());
        MultipartFile file = new MockMultipartFile(
            "file",
            "malformed.zip",
            "application/zip",
            Files.readAllBytes(malformedPath)
        );

        // When/Then
        assertThrows(RuntimeException.class, () -> service.processDatabase(file));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "test.rar",
        "test.7z",
        "test.tar.gz"
    })
    void shouldRejectUnsupportedFileTypes(String filename) {
        // Given
        MultipartFile file = new MockMultipartFile(
            "file",
            filename,
            "application/octet-stream",
            "test data".getBytes()
        );

        // When/Then
        assertThrows(RuntimeException.class, () -> service.processDatabase(file));
    }

    @Test
    void shouldHandleInvalidDatabaseStructure() throws IOException {
        // Given
        Path invalidDbPath = tempDir.resolve("invalid.zip");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(invalidDbPath.toFile()))) {
            ZipEntry entry = new ZipEntry("invalid/data.txt");
            zos.putNextEntry(entry);
            zos.write("not a rocksdb file".getBytes());
            zos.closeEntry();
        }

        MultipartFile file = new MockMultipartFile(
            "file",
            "invalid.zip",
            "application/zip",
            Files.readAllBytes(invalidDbPath)
        );

        // When/Then
        assertThrows(RuntimeException.class, () -> service.processDatabase(file));
    }
} 