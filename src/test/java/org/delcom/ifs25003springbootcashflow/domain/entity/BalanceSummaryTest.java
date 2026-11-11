package org.delcom.ifs25003springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BalanceSummaryTest {

    @Test
    void computesTotalsAndBalance() {
        BalanceSummary summary = BalanceSummary.of(List.of(
                new Transaction(1, 1, "Gaji", 5_000_000L, TransactionType.INCOME),
                new Transaction(2, 1, "Makan", 25_000L, TransactionType.EXPENSE),
                new Transaction(3, 1, "Bonus", 1_000_000L, TransactionType.INCOME)
        ));
        assertEquals(3, summary.totalTransactions());
        assertEquals(6_000_000L, summary.totalIncome());
        assertEquals(25_000L, summary.totalExpense());
        assertEquals(5_975_000L, summary.balance());
    }

    @Test
    void emptyAndNegativeBalance() {
        BalanceSummary empty = BalanceSummary.of(List.of());
        assertEquals(new BalanceSummary(0, 0, 0), empty);
        assertEquals(0, empty.balance());

        BalanceSummary negative = new BalanceSummary(1, 0, 10);
        assertEquals(-10, negative.balance());
        assertNotEquals(empty, negative);
        assertEquals(empty.hashCode(), new BalanceSummary(0, 0, 0).hashCode());
        assertTrue(negative.toString().contains("totalExpense=10"));
    }
}
