package dev.rocksdb.explorer.util;

import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import java.nio.file.Path;
import java.io.FileOutputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.io.IOException;

public class TestUtils {
    
    public static Path createTestDatabase(Path tempDir) throws Exception {
        Path dbPath = tempDir.resolve("test-db");
        Path zipPath = tempDir.resolve("test-rocksdb-small.zip");
        dbPath.toFile().mkdirs();

        // Create a small test database
        try (final Options options = new Options().setCreateIfMissing(true);
             final RocksDB db = RocksDB.open(options, dbPath.toString())) {
            
            // Add a user entry
            String userKey = "user:000001";
            String userValue = """
                {
                    "id": 1,
                    "name": "User 1",
                    "email": "user1@example.com",
                    "created": "2024-01-01",
                    "profile": {
                        "address": "123 Main St, City 1, Country",
                        "phone": "+1-555-000001",
                        "preferences": {
                            "theme": "dark",
                            "language": "en-US",
                            "notifications": true
                        },
                        "tags": ["tag1", "tag2", "tag3"]
                    },
                    "stats": {
                        "lastLogin": "2024-02-09T12:34:56Z",
                        "loginCount": 10,
                        "dataUsage": 1024
                    }
                }""";
            db.put(userKey.getBytes(), userValue.getBytes());

            // Add the nested preferences as a separate entry
            String prefsKey = "user:000001.profile.preferences";
            String prefsValue = """
                {
                    "theme": "dark",
                    "language": "en-US",
                    "notifications": true
                }""";
            db.put(prefsKey.getBytes(), prefsValue.getBytes());
        }

        // Create zip file
        try (FileOutputStream fos = new FileOutputStream(zipPath.toFile());
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            zipDirectory(dbPath.toFile(), dbPath.getFileName().toString(), zos);
        }

        return zipPath;
    }

    private static void zipDirectory(java.io.File folder, String baseName, ZipOutputStream zos) throws IOException {
        java.io.File[] files = folder.listFiles();
        if (files != null) {
            for (java.io.File file : files) {
                if (file.isDirectory()) {
                    zipDirectory(file, baseName + "/" + file.getName(), zos);
                } else {
                    ZipEntry entry = new ZipEntry(baseName + "/" + file.getName());
                    zos.putNextEntry(entry);
                    zos.write(java.nio.file.Files.readAllBytes(file.toPath()));
                    zos.closeEntry();
                }
            }
        }
    }
} 