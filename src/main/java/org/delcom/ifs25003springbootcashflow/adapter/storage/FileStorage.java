package org.delcom.ifs25003springbootcashflow.adapter.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

/**
 * Menyimpan file upload (foto profil / bukti transaksi) ke folder lokal UPLOAD_DIR.
 * Nama file dibuat unik (UUID + ekstensi) dan yang dikembalikan adalah path relatif.
 */
public class FileStorage {
    /** Sub-folder untuk foto profil. */
    public static final String PROFILES = "profiles";

    /** Sub-folder untuk bukti transaksi. */
    public static final String PROOFS = "proofs";

    private final Path root;

    public FileStorage(String uploadDir) {
        this.root = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    public Path root() {
        return root;
    }

    public String store(String folder, String originalFilename, InputStream content) throws IOException {
        Path directory = root.resolve(folder);
        Files.createDirectories(directory);

        String filename = UUID.randomUUID() + extensionOf(originalFilename);
        Path target = directory.resolve(filename);
        Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);
        return folder + "/" + filename;
    }

    public Path resolve(String relativePath) {
        return root.resolve(relativePath).normalize();
    }

    private static String extensionOf(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return ".bin";
        }
        String ext = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase(Locale.ROOT);
        if (ext.length() > 10) {
            return ".bin";
        }
        return ext;
    }
}
