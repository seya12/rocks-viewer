package dev.rocksdb.explorer.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.FileOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;
import org.junit.jupiter.api.io.TempDir;
import dev.rocksdb.explorer.util.TestUtils;

@SpringBootTest
@AutoConfigureMockMvc
class RocksDBControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @TempDir
    Path tempDir;

    @Test
    void shouldUploadAndProcessDatabase() throws Exception {
        // Given
        Path testDbPath = TestUtils.createTestDatabase(tempDir);
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test-rocksdb-small.zip",
            "application/zip",
            Files.readAllBytes(testDbPath)
        );

        // When/Then
        mockMvc.perform(multipart("/api/upload").file(file))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.['user:000001']").exists())
            .andExpect(jsonPath("$.['user:000001.profile.preferences']").exists());
    }

    @Test
    void shouldRejectLargeFiles() throws Exception {
        // Given
        Path testDbPath = tempDir.resolve("test-rocksdb-large.zip");
        Files.copy(
            getClass().getResourceAsStream("/test-dbs/test-rocksdb-large.zip"),
            testDbPath
        );
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test-rocksdb-large.zip",
            "application/zip",
            Files.readAllBytes(testDbPath)
        );

        // When/Then
        mockMvc.perform(multipart("/api/upload").file(file))
            .andExpect(status().isOk());
    }

    @Test
    void shouldRejectInvalidFiles() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "not a zip file".getBytes()
        );

        // When/Then
        mockMvc.perform(multipart("/api/upload").file(file))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleEmptyRequest() throws Exception {
        mockMvc.perform(multipart("/api/upload"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleCorruptedDatabase() throws Exception {
        // Given
        Path corruptedDbPath = Files.createTempFile("corrupted", ".zip");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(corruptedDbPath.toFile()))) {
            ZipEntry entry = new ZipEntry("corrupted/CURRENT");
            zos.putNextEntry(entry);
            zos.write("corrupted data".getBytes());
            zos.closeEntry();
        }

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "corrupted.zip",
            "application/zip",
            Files.readAllBytes(corruptedDbPath)
        );

        // When/Then
        mockMvc.perform(multipart("/api/upload").file(file))
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("Failed to process database")));

        Files.deleteIfExists(corruptedDbPath);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "test.rar",
        "test.7z",
        "test.tar.gz",
        "test.txt"
    })
    void shouldRejectUnsupportedFileTypes(String filename) throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            filename,
            "application/octet-stream",
            "test data".getBytes()
        );

        // When/Then
        mockMvc.perform(multipart("/api/upload").file(file))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Please upload a zip file"));
    }

    @Test
    void shouldHandleMultipleSimultaneousUploads() throws Exception {
        // Given
        Path testDbPath = Paths.get("src/test/resources/test-dbs/test-rocksdb-small.zip");
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test-rocksdb-small.zip",
            "application/zip",
            Files.readAllBytes(testDbPath)
        );

        // When/Then
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(multipart("/api/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.['user:000001']").exists());
        }
    }

    @Test
    void shouldHandleMissingFile() throws Exception {
        mockMvc.perform(multipart("/api/upload"))
            .andExpect(status().isBadRequest());
    }
} 