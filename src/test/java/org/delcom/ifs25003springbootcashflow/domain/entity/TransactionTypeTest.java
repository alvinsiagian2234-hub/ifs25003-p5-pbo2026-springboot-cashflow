package org.delcom.ifs25003springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransactionTypeTest {

    @Test
    void labelsAndSigns() {
        assertEquals("Pemasukan", TransactionType.INCOME.label());
        assertEquals("Pengeluaran", TransactionType.EXPENSE.label());
        assertEquals(1, TransactionType.INCOME.sign());
        assertEquals(-1, TransactionType.EXPENSE.sign());
        assertEquals(TransactionType.EXPENSE, TransactionType.valueOf("EXPENSE"));
        assertEquals(2, TransactionType.values().length);
    }
}
