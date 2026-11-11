package org.delcom.ifs25003springbootcashflow.adapter.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileStorageTest {

    @TempDir
    Path tempDir;

    @Test
    void storesProofWithUniqueNameAndResolvesPath() throws Exception {
        FileStorage storage = new FileStorage(tempDir.toString());
        String first = storage.store(FileStorage.PROOFS, "Bukti.PNG",
                new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8)));
        String second = storage.store(FileStorage.PROOFS, "Bukti.PNG",
                new ByteArrayInputStream("def".getBytes(StandardCharsets.UTF_8)));

        assertTrue(first.startsWith("proofs/"));
        assertTrue(first.endsWith(".png"));
        assertNotEquals(first, second);
        assertEquals("abc", Files.readString(storage.resolve(first)));
        assertEquals(tempDir.toAbsolutePath().normalize(), storage.root());
    }

    @Test
    void fallsBackExtensionWhenMissingOrTooLong() throws Exception {
        FileStorage storage = new FileStorage(tempDir.toString());
        assertTrue(storage.store(FileStorage.PROFILES, "tanpaext", new ByteArrayInputStream(new byte[]{1}))
                .endsWith(".bin"));
        assertTrue(storage.store(FileStorage.PROFILES, "a." + "x".repeat(20), new ByteArrayInputStream(new byte[]{1}))
                .endsWith(".bin"));
        assertTrue(storage.store(FileStorage.PROFILES, null, new ByteArrayInputStream(new byte[]{1}))
                .endsWith(".bin"));
    }
}
