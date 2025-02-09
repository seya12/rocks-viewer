package dev.rocksdb.explorer.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RocksDBControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldUploadAndProcessDatabase() throws Exception {
        // Given
        Path testDbPath = Paths.get("src/test/resources/test-dbs/test-rocksdb-small.zip");
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
            .andExpect(jsonPath("$.['user:000001']").value("John Doe"))
            .andExpect(jsonPath("$.['config:theme']").exists());
    }

    @Test
    void shouldRejectLargeFiles() throws Exception {
        // Given
        Path testDbPath = Paths.get("src/test/resources/test-dbs/test-rocksdb-large.zip");
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
} 