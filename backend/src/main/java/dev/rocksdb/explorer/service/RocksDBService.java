package dev.rocksdb.explorer.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipInputStream;

import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RocksDBService {
    private static final Logger log = LoggerFactory.getLogger(RocksDBService.class);
    
    static {
        RocksDB.loadLibrary();
    }

    public Map<String, String> processDatabase(MultipartFile file) throws IOException {
        String sessionId = UUID.randomUUID().toString();
        Path extractPath = Files.createTempDirectory("rocksdb-" + sessionId);
        log.info("Extracting to: {}", extractPath);
        
        // Extract zip file
        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            var entry = zis.getNextEntry();
            while (entry != null) {
                Path filePath = extractPath.resolve(entry.getName());
                if (!entry.isDirectory()) {
                    Files.createDirectories(filePath.getParent());
                    Files.copy(zis, filePath);
                }
                entry = zis.getNextEntry();
            }
        }

        // Find the directory containing the RocksDB files
        Path dbPath = findRocksDBDirectory(extractPath);
        log.info("Found RocksDB directory at: {}", dbPath);

        // Read database
        Map<String, String> keyValues = new HashMap<>();
        try (final Options options = new Options().setCreateIfMissing(false);
             final RocksDB db = RocksDB.openReadOnly(options, dbPath.toString())) {
            
            try (RocksIterator iter = db.newIterator()) {
                for (iter.seekToFirst(); iter.isValid(); iter.next()) {
                    String key = new String(iter.key());
                    String value = new String(iter.value());
                    keyValues.put(key, value);
                }
            }
        } catch (Exception e) {
            log.error("Error reading RocksDB database", e);
            throw new RuntimeException("Failed to read RocksDB database", e);
        } finally {
            // Cleanup
            deleteDirectory(extractPath.toFile());
        }

        return keyValues;
    }

    private Path findRocksDBDirectory(Path root) throws IOException {
        // Look for common RocksDB files like CURRENT, MANIFEST, or *.log
        try (var files = Files.walk(root)) {
            return files
                .filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().equals("CURRENT") 
                    || p.getFileName().toString().startsWith("MANIFEST"))
                .map(Path::getParent)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No RocksDB files found in the archive"));
        }
    }

    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }
} 