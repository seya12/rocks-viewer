package dev.rocksdb.explorer.util;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.rocksdb.Options;
import org.rocksdb.RocksDB;

public class TestDatabaseCreator {
    public static void main(String[] args) throws Exception {
        createTestDatabase("small", 5);
        createTestDatabase("medium", 10_000);      // ~2MB
        createTestDatabase("large", 100_000);      // ~20MB
    }

    private static void createTestDatabase(String size, int entries) throws Exception {
        System.out.println("Creating " + size + " database with " + entries + " entries...");
        Path dbPath = Path.of("test-db-" + size);
        dbPath.toFile().mkdirs();

        try (final Options options = new Options();
             final RocksDB db = RocksDB.open(options.setCreateIfMissing(true), dbPath.toString())) {
            // Create entries with different patterns
            for (int i = 1; i <= entries; i++) {
                // Users (20% of entries)
                if (i <= entries * 0.2) {
                    String key = String.format("user:%06d", i);
                    String value = String.format(
                        "{" +
                        "\"id\":%d," +
                        "\"name\":\"User %d\"," +
                        "\"email\":\"user%d@example.com\"," +
                        "\"created\":\"2024-01-01\"," +
                        "\"profile\":{" +
                            "\"address\":\"123 Main St, City %d, Country\"," +
                            "\"phone\":\"+1-555-%06d\"," +
                            "\"preferences\":{" +
                                "\"theme\":\"dark\"," +
                                "\"language\":\"en-US\"," +
                                "\"notifications\":true" +
                            "}," +
                            "\"tags\":[\"tag1\",\"tag2\",\"tag3\"]" +
                        "}," +
                        "\"stats\":{" +
                            "\"lastLogin\":\"2024-02-09T12:34:56Z\"," +
                            "\"loginCount\":%d," +
                            "\"dataUsage\":%d" +
                        "}" +
                        "}",
                        i, i, i, i, i, i * 10, i * 1024
                    );
                    db.put(key.getBytes(), value.getBytes());
                }
                // Metrics (40% of entries)
                else if (i <= entries * 0.6) {
                    Instant timestamp = Instant.now().minus(i, ChronoUnit.MINUTES);
                    String key = String.format("metric:%s", timestamp);
                    String value = String.format(
                        "{" +
                        "\"timestamp\":\"%s\"," +
                        "\"metrics\":{" +
                            "\"cpu\":%.2f," +
                            "\"memory\":%.2f," +
                            "\"disk\":%.2f," +
                            "\"network\":%.2f" +
                        "}," +
                        "\"tags\":{" +
                            "\"host\":\"server-%d\"," +
                            "\"region\":\"region-%d\"," +
                            "\"environment\":\"prod\"" +
                        "}," +
                        "\"metadata\":{" +
                            "\"version\":\"1.2.3\"," +
                            "\"collector\":\"agent-v2\"," +
                            "\"samplingRate\":60" +
                        "}" +
                        "}",
                        timestamp,
                        Math.random() * 100,
                        Math.random() * 16384,
                        Math.random() * 1024,
                        Math.random() * 1000,
                        i % 100,
                        i % 10
                    );
                    db.put(key.getBytes(), value.getBytes());
                }
                // Logs (40% of entries)
                else {
                    String key = String.format("log:%d:%06d", System.currentTimeMillis(), i);
                    String value = String.format(
                        "{" +
                        "\"timestamp\":\"%s\"," +
                        "\"level\":\"INFO\"," +
                        "\"logger\":\"com.example.service.%d\"," +
                        "\"thread\":\"pool-%d-thread-%d\"," +
                        "\"message\":\"Detailed log message with ID %d and additional context information\"," +
                        "\"context\":{" +
                            "\"requestId\":\"req-%d\"," +
                            "\"userId\":%d," +
                            "\"duration\":%.2f," +
                            "\"status\":200" +
                        "}," +
                        "\"stackTrace\":\"com.example.service.Handler.process(Handler.java:%d)\\n" +
                            "com.example.service.Worker.handle(Worker.java:42)\\n" +
                            "com.example.service.Main.execute(Main.java:123)\"" +
                        "}",
                        Instant.now(),
                        i % 10,
                        i % 5,
                        i % 20,
                        i,
                        i * 1000,
                        i % 1000,
                        Math.random() * 1000,
                        i % 500
                    );
                    db.put(key.getBytes(), value.getBytes());
                }

                if (i % 1000 == 0) {
                    System.out.println("Created " + i + " entries");
                }
            }
        }

        // Create zip file
        try (FileOutputStream fos = new FileOutputStream("test-rocksdb-" + size + ".zip");
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            zipDirectory(dbPath.toFile(), dbPath.toFile().getName(), zos);
        }

        // Cleanup
        deleteDirectory(dbPath.toFile());
        System.out.println(size + " database created successfully!");
    }

    private static void zipDirectory(File folder, String baseName, ZipOutputStream zos) throws Exception {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
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

    private static void deleteDirectory(File directory) {
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