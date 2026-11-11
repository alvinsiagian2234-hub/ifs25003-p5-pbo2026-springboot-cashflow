package org.delcom.ifs25003springbootcashflow.adapter.repository;

import org.delcom.ifs25003springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25003springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionRepositoryTest {

    private TransactionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new TransactionRepository();
    }

    @Test
    void saveFindUpdateDelete() {
        Transaction first = repository.save(1, "Gaji", 100L, TransactionType.INCOME);
        Transaction second = repository.save(2, "Kopi", 10L, TransactionType.EXPENSE);
        assertEquals(1, first.getId());
        assertEquals(2, second.getId());

        assertEquals(2, repository.findAll().size());
        assertEquals(1, repository.findByUserId(1).size());
        assertTrue(repository.findById(2).isPresent());
        assertTrue(repository.findById(3).isEmpty());
        assertEquals(1, repository.findBy(t -> t.getAmount() > 50).size());

        first.changeAmount(150L);
        repository.update(first);
        assertEquals(150L, repository.findById(1).orElseThrow().getAmount());

        // salinan defensif: list hasil findAll tidak memengaruhi data internal
        repository.findAll().clear();
        assertEquals(2, repository.findAll().size());

        assertTrue(repository.deleteById(1));
        assertFalse(repository.deleteById(1));
        assertTrue(repository.findByUserId(1).isEmpty());
    }
}
