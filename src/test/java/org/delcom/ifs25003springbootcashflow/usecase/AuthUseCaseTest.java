package org.delcom.ifs25003springbootcashflow.usecase;

import org.delcom.ifs25003springbootcashflow.adapter.repository.UserRepository;
import org.delcom.ifs25003springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25003springbootcashflow.domain.entity.Role;
import org.delcom.ifs25003springbootcashflow.domain.entity.User;
import org.delcom.ifs25003springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthUseCaseTest {

    private AuthUseCase authUseCase;

    @BeforeEach
    void setUp() {
        authUseCase = new AuthUseCase(new UserRepository());
    }

    @Test
    void registerLoginAndRejectDuplicates() throws Exception {
        User user = authUseCase.register("  andi ", "secret", " Andi ");
        assertEquals(Role.USER, user.getRole());
        assertEquals("andi", user.getUsername());
        assertEquals("Andi", user.getFullName());
        assertEquals(PasswordHasher.hash("secret"), user.getPasswordHash());

        assertEquals(user.getId(), authUseCase.login("ANDI", "secret").getId());

        assertThrows(ValidationException.class, () -> authUseCase.register("andi", "secret", "Andi"));
        assertThrows(ValidationException.class, () -> authUseCase.login("andi", "salah"));
        assertThrows(ValidationException.class, () -> authUseCase.login("tidakada", "secret"));
    }

    @Test
    void registerValidation() {
        assertThrows(ValidationException.class, () -> authUseCase.register(null, "secret", "Nama"));
        assertThrows(ValidationException.class, () -> authUseCase.register(" ", "secret", "Nama"));
        assertThrows(ValidationException.class, () -> authUseCase.register("user", null, "Nama"));
        assertThrows(ValidationException.class, () -> authUseCase.register("user", "abc", "Nama"));
        assertThrows(ValidationException.class, () -> authUseCase.register("user", "secret", null));
        assertThrows(ValidationException.class, () -> authUseCase.register("user", "secret", " "));
    }

    @Test
    void loginValidation() {
        assertThrows(ValidationException.class, () -> authUseCase.login(null, "x"));
        assertThrows(ValidationException.class, () -> authUseCase.login(" ", "x"));
        assertThrows(ValidationException.class, () -> authUseCase.login("user", null));
        assertThrows(ValidationException.class, () -> authUseCase.login("user", ""));
    }

    @Test
    void ensureRootAdminCreatesOnce() throws Exception {
        User root = authUseCase.ensureRootAdmin("admin", "admin");
        assertEquals(Role.ADMIN, root.getRole());
        assertTrue(root.isAdmin());
        assertEquals(root.getId(), authUseCase.ensureRootAdmin("admin", "admin").getId());
        assertEquals(root.getId(), authUseCase.login("admin", "admin").getId());

        assertThrows(ValidationException.class, () -> authUseCase.ensureRootAdmin(null, "x"));
        assertThrows(ValidationException.class, () -> authUseCase.ensureRootAdmin(" ", "x"));
        assertThrows(ValidationException.class, () -> authUseCase.ensureRootAdmin("admin", null));
        assertThrows(ValidationException.class, () -> authUseCase.ensureRootAdmin("admin", ""));
    }
}
