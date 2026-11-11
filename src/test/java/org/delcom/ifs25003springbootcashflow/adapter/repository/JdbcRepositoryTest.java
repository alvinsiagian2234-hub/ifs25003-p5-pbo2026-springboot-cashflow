package org.delcom.ifs25003springbootcashflow.adapter.repository;

import org.delcom.ifs25003springbootcashflow.domain.entity.Role;
import org.delcom.ifs25003springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25003springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25003springbootcashflow.domain.entity.User;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tes repository JDBC memakai H2 (mode PostgreSQL).
 */
class JdbcRepositoryTest {

    private DataSource dataSource;
    private JdbcUserRepository userRepository;
    private JdbcTransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        dataSource = createDataSource();
        SchemaInitializer.initialize(dataSource);
        userRepository = new JdbcUserRepository(dataSource);
        transactionRepository = new JdbcTransactionRepository(dataSource);
    }

    private DataSource createDataSource() {
        JdbcDataSource h2 = new JdbcDataSource();
        h2.setURL("jdbc:h2:mem:jdbc_repo_" + System.nanoTime()
                + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE");
        h2.setUser("sa");
        h2.setPassword("");
        return h2;
    }

    @Test
    void schemaInitializerIsIdempotent() {
        SchemaInitializer.initialize(dataSource);
        assertTrue(transactionRepository.findAll().isEmpty());
    }

    @Test
    void userCrudAndFinders() {
        User saved = userRepository.saveNew("andi", "hash", "Andi", Role.USER);
        assertTrue(saved.getId() > 0);

        assertTrue(userRepository.findByUsername("ANDI").isPresent());
        assertEquals(1, userRepository.findAll().size());
        assertTrue(userRepository.findById(saved.getId()).isPresent());
        assertEquals(1, userRepository.findBy(user -> user.getUsername().equals("andi")).size());

        saved.changeFullName("Andi Baru");
        saved.changePhotoPath("profiles/a.png");
        userRepository.update(saved);
        User reloaded = userRepository.findById(saved.getId()).orElseThrow();
        assertEquals("Andi Baru", reloaded.getFullName());
        assertEquals("profiles/a.png", reloaded.getPhotoPath());
        assertEquals(Role.USER, reloaded.getRole());

        User viaSave = userRepository.save(new User(0, "budi", "h", "Budi", Role.ADMIN, null));
        assertTrue(viaSave.getId() > 0);
        assertTrue(userRepository.findById(viaSave.getId()).orElseThrow().isAdmin());

        assertTrue(userRepository.deleteById(viaSave.getId()));
        assertFalse(userRepository.deleteById(viaSave.getId()));
        assertTrue(userRepository.findByUsername("tidakada").isEmpty());
        assertTrue(userRepository.findById(999).isEmpty());
    }

    @Test
    void usernameMustBeUnique() {
        userRepository.saveNew("andi", "hash", "Andi", Role.USER);
        assertThrows(IllegalStateException.class, () -> userRepository.saveNew("andi", "h", "A", Role.USER));
    }

    @Test
    void transactionCrudAndFinders() {
        User owner = userRepository.saveNew("owner", "hash", "Owner", Role.USER);
        User other = userRepository.saveNew("other", "hash", "Other", Role.USER);

        Transaction income = transactionRepository.save(owner.getId(), "Gaji", 5_000_000L, TransactionType.INCOME);
        assertTrue(income.getId() > 0);
        assertEquals(5_000_000L, income.getAmount());
        assertNull(income.getProofPath());

        Transaction viaEntity = transactionRepository.save(
                new Transaction(0, owner.getId(), "Makan", 25_000L, TransactionType.EXPENSE));
        assertTrue(viaEntity.getId() > income.getId());
        transactionRepository.save(other.getId(), "Lain", 1L, TransactionType.EXPENSE);

        assertEquals(3, transactionRepository.findAll().size());
        assertEquals(2, transactionRepository.findByUserId(owner.getId()).size());
        assertEquals(1, transactionRepository.findBy(t -> t.getDescription().equals("Gaji")).size());
        assertTrue(transactionRepository.findById(999).isEmpty());

        Transaction loaded = transactionRepository.findById(income.getId()).orElseThrow();
        assertEquals(income.getCreatedAt(), loaded.getCreatedAt());
        assertEquals(TransactionType.INCOME, loaded.getType());
        assertEquals(owner.getId(), loaded.getUserId());

        loaded.changeDescription("Gaji Mei");
        loaded.changeAmount(6_000_000L);
        loaded.changeType(TransactionType.EXPENSE);
        loaded.changeProofPath("proofs/x.png");
        transactionRepository.update(loaded);
        Transaction updated = transactionRepository.findById(income.getId()).orElseThrow();
        assertEquals("Gaji Mei", updated.getDescription());
        assertEquals(6_000_000L, updated.getAmount());
        assertEquals(TransactionType.EXPENSE, updated.getType());
        assertEquals("proofs/x.png", updated.getProofPath());

        assertTrue(transactionRepository.deleteById(income.getId()));
        assertFalse(transactionRepository.deleteById(income.getId()));
        assertEquals(1, transactionRepository.findByUserId(owner.getId()).size());
    }

    @Test
    void transactionRequiresExistingUser() {
        assertThrows(IllegalStateException.class,
                () -> transactionRepository.save(12345, "Tanpa user", 1L, TransactionType.INCOME));
    }
}
