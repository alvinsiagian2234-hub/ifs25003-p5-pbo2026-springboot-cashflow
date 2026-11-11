package org.delcom.ifs25003springbootcashflow.domain;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

class PasswordHasherTest {

    @Test
    void hashIsDeterministicAndMatches() {
        String hash = PasswordHasher.hash("secret");
        assertEquals(64, hash.length());
        assertTrue(PasswordHasher.matches("secret", hash));
        assertFalse(PasswordHasher.matches("lain", hash));
    }

    @Test
    void messageDigestFailsWhenAlgorithmMissing() {
        try (MockedStatic<MessageDigest> mocked = mockStatic(MessageDigest.class)) {
            mocked.when(() -> MessageDigest.getInstance("SHA-256"))
                    .thenThrow(new NoSuchAlgorithmException("missing"));
            assertThrows(IllegalStateException.class, PasswordHasher::messageDigest);
        }
    }
}
