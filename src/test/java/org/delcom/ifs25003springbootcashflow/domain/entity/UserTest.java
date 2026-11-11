package org.delcom.ifs25003springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    @Test
    void mutatorsAndAdminFlag() {
        User user = new User(1, "andi", "hash", "Andi", Role.USER, null);
        assertFalse(user.isAdmin());
        assertEquals(Role.USER, user.getRole());

        user.changeFullName("Andi Baru");
        user.changePasswordHash("hash2");
        user.changePhotoPath("profiles/a.png");

        assertEquals(1, user.getId());
        assertEquals("andi", user.getUsername());
        assertEquals("Andi Baru", user.getFullName());
        assertEquals("hash2", user.getPasswordHash());
        assertEquals("profiles/a.png", user.getPhotoPath());

        assertTrue(new User(2, "admin", "h", "Admin", Role.ADMIN, null).isAdmin());
    }
}
