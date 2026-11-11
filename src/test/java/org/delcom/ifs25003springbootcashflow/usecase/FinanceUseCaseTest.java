package org.delcom.ifs25003springbootcashflow.usecase;

import org.delcom.ifs25003springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25003springbootcashflow.domain.entity.BalanceSummary;
import org.delcom.ifs25003springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25003springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25003springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25003springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25003springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinanceUseCaseTest {

    private static final int ANDI = 1;
    private static final int BUDI = 2;

    private FinanceUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FinanceUseCase(new TransactionRepository());
    }

    @Test
    void incomeAddsAndExpenseSubtractsBalance() throws Exception {
        Transaction salary = useCase.addIncome(ANDI, "  Gaji  ", "5000000");
        Transaction food = useCase.addExpense(ANDI, "Makan", "25.000");
        useCase.addIncome(BUDI, "Uang saku", "100,000");

        assertEquals("Gaji", salary.getDescription());
        assertEquals(TransactionType.INCOME, salary.getType());
        assertEquals(TransactionType.EXPENSE, food.getType());
        assertEquals(25_000L, food.getAmount());

        assertEquals(4_975_000L, useCase.getBalance(ANDI));
        assertEquals(100_000L, useCase.getBalance(BUDI));

        BalanceSummary summary = useCase.getSummary(ANDI);
        assertEquals(2, summary.totalTransactions());
        assertEquals(5_000_000L, summary.totalIncome());
        assertEquals(25_000L, summary.totalExpense());

        assertEquals(2, useCase.getTransactionsByUser(ANDI).size());
        assertEquals(3, useCase.getAllTransactions().size());

        useCase.removeTransactionForUser(ANDI, food.getId());
        assertEquals(5_000_000L, useCase.getBalance(ANDI));
    }

    @Test
    void addTransactionValidation() {
        assertThrows(ValidationException.class, () -> useCase.addIncome(ANDI, null, "10"));
        assertThrows(ValidationException.class, () -> useCase.addIncome(ANDI, "   ", "10"));
        assertThrows(ValidationException.class, () -> useCase.addExpense(ANDI, "Ok", "0"));
        assertThrows(ValidationException.class, () -> useCase.addTransaction(ANDI, "Ok", -5, TransactionType.INCOME));
        assertThrows(ValidationException.class, () -> useCase.addTransaction(ANDI, "Ok", 0, TransactionType.EXPENSE));
        ValidationException noType = assertThrows(ValidationException.class,
                () -> useCase.addTransaction(ANDI, "Ok", 10, null));
        assertEquals("Jenis transaksi tidak boleh kosong", noType.getMessage());
        assertTrue(useCase.getAllTransactions().isEmpty());
    }

    @Test
    void parseAmountAcceptsPlainAndGroupedNumbers() throws Exception {
        assertEquals(1_000_000L, useCase.parseAmount("1000000"));
        assertEquals(1_000_000L, useCase.parseAmount(" 1.000.000 "));
        assertEquals(1_000_000L, useCase.parseAmount("1,000,000"));
        assertEquals(1_500L, useCase.parseAmount("1 500"));
    }

    @Test
    void parseAmountRejectsInvalidInput() {
        assertMessage("Jumlah tidak boleh kosong", () -> useCase.parseAmount(null));
        assertMessage("Jumlah tidak boleh kosong", () -> useCase.parseAmount("   "));
        assertMessage("Jumlah harus berupa angka", () -> useCase.parseAmount("abc"));
        assertMessage("Jumlah harus berupa angka", () -> useCase.parseAmount("12a"));
        assertMessage("Jumlah harus berupa angka", () -> useCase.parseAmount("1.5"));
        assertMessage("Jumlah harus berupa angka", () -> useCase.parseAmount("-abc"));
        assertMessage("Jumlah harus lebih dari 0", () -> useCase.parseAmount("-5000"));
        assertMessage("Jumlah harus lebih dari 0", () -> useCase.parseAmount("0"));
        assertMessage("Jumlah harus lebih dari 0", () -> useCase.parseAmount("000"));
        assertMessage("Jumlah terlalu besar", () -> useCase.parseAmount("99999999999999999999"));
    }

    @Test
    void parseIdValidation() throws Exception {
        assertEquals(12, useCase.parseId(" 12 "));
        assertMessage("ID tidak valid", () -> useCase.parseId(null));
        assertMessage("ID tidak valid", () -> useCase.parseId(" "));
        assertMessage("ID tidak valid", () -> useCase.parseId("abc"));
        assertMessage("ID tidak valid", () -> useCase.parseId("0"));
        assertMessage("ID tidak valid", () -> useCase.parseId("-3"));
    }

    @Test
    void removeValidatesIdExistenceAndOwnership() throws Exception {
        Transaction mine = useCase.addIncome(ANDI, "Gaji", "100");

        assertMessage("ID tidak valid", () -> useCase.removeTransaction(0));
        EntityNotFoundException notFound = assertThrows(EntityNotFoundException.class,
                () -> useCase.removeTransaction(99));
        assertEquals(99, notFound.getId());
        assertEquals("Transaksi dengan ID 99 tidak ditemukan.", notFound.getMessage());

        assertThrows(EntityNotFoundException.class, () -> useCase.removeTransactionForUser(ANDI, 99));
        assertMessage("ID tidak valid", () -> useCase.removeTransactionForUser(ANDI, -1));
        assertMessage("Transaksi tidak milik pengguna ini",
                () -> useCase.removeTransactionForUser(BUDI, mine.getId()));
        assertEquals(1, useCase.getTransactionsByUser(ANDI).size());

        useCase.removeTransaction(mine.getId());
        assertTrue(useCase.getTransactionsByUser(ANDI).isEmpty());
    }

    @Test
    void updateChangesOnlyProvidedFields() throws Exception {
        Transaction tx = useCase.addExpense(ANDI, "Makan", "20000");

        Transaction same = useCase.updateTransactionForUser(ANDI, tx.getId(), null, null, null, null, false);
        assertSame(tx, same);
        assertEquals("Makan", tx.getDescription());
        assertEquals(20_000L, tx.getAmount());
        assertEquals(TransactionType.EXPENSE, tx.getType());

        useCase.updateTransactionForUser(ANDI, tx.getId(), " Makan malam ", "30.000", TransactionType.INCOME,
                "proofs/a.png", false);
        assertEquals("Makan malam", tx.getDescription());
        assertEquals(30_000L, tx.getAmount());
        assertEquals(TransactionType.INCOME, tx.getType());
        assertEquals("proofs/a.png", tx.getProofPath());

        useCase.updateTransactionForUser(ANDI, tx.getId(), null, null, null, "proofs/b.png", true);
        assertNull(tx.getProofPath());

        assertEquals(tx, useCase.getTransactionForUser(ANDI, tx.getId()));
    }

    @Test
    void updateValidationDoesNotPartiallyApply() throws Exception {
        Transaction tx = useCase.addIncome(ANDI, "Gaji", "100");

        assertMessage("Keterangan tidak boleh kosong",
                () -> useCase.updateTransactionForUser(ANDI, tx.getId(), " ", "200", null, null, false));
        assertMessage("Jumlah harus berupa angka",
                () -> useCase.updateTransactionForUser(ANDI, tx.getId(), "Baru", "x", null, null, false));
        assertEquals("Gaji", tx.getDescription());
        assertEquals(100L, tx.getAmount());

        assertThrows(EntityNotFoundException.class,
                () -> useCase.updateTransactionForUser(ANDI, 99, "A", "1", null, null, false));
        assertMessage("Transaksi tidak milik pengguna ini",
                () -> useCase.updateTransactionForUser(BUDI, tx.getId(), "A", "1", null, null, false));
        assertMessage("Transaksi tidak milik pengguna ini", () -> useCase.getTransactionForUser(BUDI, tx.getId()));
    }

    @Test
    void searchIsCaseInsensitiveAndScopedToUser() throws Exception {
        useCase.addIncome(ANDI, "Gaji Bulanan", "100");
        useCase.addExpense(ANDI, "Beli kopi", "10");
        useCase.addIncome(BUDI, "Gaji Budi", "100");

        List<Transaction> result = useCase.searchTransactions(ANDI, "  GAJI ");
        assertEquals(1, result.size());
        assertEquals("Gaji Bulanan", result.get(0).getDescription());

        assertEquals(2, useCase.searchTransactions(ANDI, null).size());
        assertEquals(2, useCase.searchTransactions(ANDI, "  ").size());
        assertTrue(useCase.searchTransactions(ANDI, "tidak ada").isEmpty());
    }

    @Test
    void sortAndFindTransactions() throws Exception {
        useCase.addIncome(ANDI, "Gaji", "5000");
        useCase.addExpense(ANDI, "Kopi", "20");
        useCase.addIncome(ANDI, "Bonus", "700");
        useCase.addExpense(BUDI, "Lain", "1");

        assertEquals(List.of(20L, 700L, 5000L), amounts(useCase.sortTransactions(ANDI, SortOption.AMOUNT_ASC)));
        assertEquals(List.of(5000L, 700L, 20L), amounts(useCase.sortTransactions(ANDI, SortOption.AMOUNT_DESC)));
        assertFalse(useCase.sortTransactions(ANDI, SortOption.EXPENSE_FIRST).get(0).isIncome());
        assertTrue(useCase.sortTransactions(ANDI, SortOption.INCOME_FIRST).get(0).isIncome());
        assertMessage("Opsi pengurutan tidak valid", () -> useCase.sortTransactions(ANDI, null));

        assertEquals(List.of(5000L, 20L, 700L), amounts(useCase.findTransactions(ANDI, "", null)));
        assertEquals(List.of(20L, 700L), amounts(useCase.findTransactions(ANDI, "O", SortOption.AMOUNT_ASC)));
    }

    private static List<Long> amounts(List<Transaction> transactions) {
        return transactions.stream().map(Transaction::getAmount).toList();
    }

    private static void assertMessage(String expected, org.junit.jupiter.api.function.Executable executable) {
        ValidationException exception = assertThrows(ValidationException.class, executable);
        assertEquals(expected, exception.getMessage());
    }
}
