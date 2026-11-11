package org.delcom.ifs25003springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SortOptionTest {

    private final LocalDateTime base = LocalDateTime.of(2026, 5, 1, 8, 0);
    private final Transaction salary = new Transaction(1, 1, "gaji", 5_000L, TransactionType.INCOME, null, base);
    private final Transaction coffee = new Transaction(2, 1, "Kopi", 20L, TransactionType.EXPENSE, null,
            base.plusHours(1));
    private final Transaction bonus = new Transaction(3, 1, "bonus", 700L, TransactionType.INCOME, null,
            base.plusHours(1));
    private final List<Transaction> all = List.of(salary, coffee, bonus);

    private List<Integer> ids(SortOption option) {
        return all.stream().sorted(option.comparator()).map(Transaction::getId).toList();
    }

    @Test
    void amountOrdering() {
        assertEquals(List.of(2, 3, 1), ids(SortOption.AMOUNT_ASC));
        assertEquals(List.of(1, 3, 2), ids(SortOption.AMOUNT_DESC));
    }

    @Test
    void typeOrdering() {
        assertEquals(List.of(1, 3, 2), ids(SortOption.INCOME_FIRST));
        assertEquals(List.of(2, 1, 3), ids(SortOption.EXPENSE_FIRST));
    }

    @Test
    void timeOrderingUsesIdAsTieBreaker() {
        assertEquals(List.of(3, 2, 1), ids(SortOption.NEWEST_FIRST));
        assertEquals(List.of(1, 2, 3), ids(SortOption.OLDEST_FIRST));
    }

    @Test
    void descriptionOrderingIsCaseInsensitive() {
        assertEquals(List.of(3, 1, 2), ids(SortOption.DESCRIPTION_ASC));
    }

    @Test
    void everyOptionHasLabel() {
        assertEquals(7, SortOption.values().length);
        for (SortOption option : SortOption.values()) {
            assertFalse(option.label().isBlank());
        }
        assertEquals(SortOption.AMOUNT_ASC, SortOption.valueOf("AMOUNT_ASC"));
    }
}
