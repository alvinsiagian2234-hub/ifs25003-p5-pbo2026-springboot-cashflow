package org.delcom.ifs25003springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionTest {

    @Test
    void shortConstructorUsesNowAndNoProof() {
        LocalDateTime before = LocalDateTime.now();
        Transaction transaction = new Transaction(1, 10, "Gaji", 5_000_000L, TransactionType.INCOME);

        assertEquals(1, transaction.getId());
        assertEquals(10, transaction.getUserId());
        assertEquals("Gaji", transaction.getDescription());
        assertEquals(5_000_000L, transaction.getAmount());
        assertEquals(TransactionType.INCOME, transaction.getType());
        assertNull(transaction.getProofPath());
        assertNotNull(transaction.getCreatedAt());
        assertFalse(transaction.getCreatedAt().isBefore(before));
        assertTrue(transaction.isIncome());
        assertFalse(transaction.hasProof());
        assertEquals(5_000_000L, transaction.signedAmount());
    }

    @Test
    void fullConstructorAndMutators() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 2, 3, 4);
        Transaction transaction = new Transaction(2, 10, "Makan", 25_000L, TransactionType.EXPENSE,
                "proofs/a.png", createdAt);

        assertEquals(createdAt, transaction.getCreatedAt());
        assertFalse(transaction.isIncome());
        assertTrue(transaction.hasProof());
        assertEquals(-25_000L, transaction.signedAmount());

        transaction.changeDescription("Makan siang");
        transaction.changeAmount(30_000L);
        transaction.changeType(TransactionType.INCOME);
        transaction.changeProofPath("   ");

        assertEquals("Makan siang", transaction.getDescription());
        assertEquals(30_000L, transaction.getAmount());
        assertTrue(transaction.isIncome());
        assertFalse(transaction.hasProof());
        assertEquals(30_000L, transaction.signedAmount());
    }
}
