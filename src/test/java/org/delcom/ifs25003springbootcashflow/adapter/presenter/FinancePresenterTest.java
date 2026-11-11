package org.delcom.ifs25003springbootcashflow.adapter.presenter;

import org.delcom.ifs25003springbootcashflow.domain.entity.BalanceSummary;
import org.delcom.ifs25003springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25003springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FinancePresenterTest {

    private final FinancePresenter presenter = new FinancePresenter();
    private final LocalDateTime time = LocalDateTime.of(2026, 3, 4, 5, 6);
    private final Transaction salary =
            new Transaction(1, 1, "Gaji", 5_000_000L, TransactionType.INCOME, null, time);
    private final Transaction food =
            new Transaction(2, 1, "Makan", 25_000L, TransactionType.EXPENSE, null, time);

    @Test
    void formatsMoneyWithThousandsSeparatorAndTwoDecimals() {
        assertEquals("Rp 1,000,000.00", presenter.formatMoney(1_000_000L));
        assertEquals("Rp 0.00", presenter.formatMoney(0));
        assertEquals("Rp 999.00", presenter.formatMoney(999));
        assertEquals("Rp -25,000.00", presenter.formatMoney(-25_000L));
    }

    @Test
    void formatsTransactionLineAndBalance() {
        assertEquals("[1] Gaji | Rp 5,000,000.00 | [Pemasukan]", presenter.format(salary));
        assertEquals("[2] Makan | Rp 25,000.00 | [Pengeluaran]", presenter.format(food));
        assertEquals("Saldo: Rp 4,975,000.00", presenter.formatBalance(4_975_000L));
        assertEquals("+Rp 5,000,000.00", presenter.formatSignedAmount(salary));
        assertEquals("-Rp 25,000.00", presenter.formatSignedAmount(food));
        assertEquals("Pemasukan", presenter.formatType(TransactionType.INCOME));
        assertEquals("04/03/2026 05:06", presenter.formatCreatedAt(salary));
    }

    @Test
    void formatsSummary() {
        assertEquals("""
                Jumlah Transaksi: 2
                Total Pemasukan: Rp 5,000,000.00
                Total Pengeluaran: Rp 25,000.00
                Saldo: Rp 4,975,000.00""", presenter.formatSummary(BalanceSummary.of(List.of(salary, food))));
    }

    @Test
    void formatsListsAndEmptyStates() {
        assertEquals("""
                Daftar Transaksi:
                [1] Gaji | Rp 5,000,000.00 | [Pemasukan]
                [2] Makan | Rp 25,000.00 | [Pengeluaran]
                Saldo: Rp 4,975,000.00""", presenter.showTransactions(List.of(salary, food), 4_975_000L));
        assertEquals("""
                Daftar Transaksi:
                - Data transaksi belum tersedia!
                Saldo: Rp 0.00""", presenter.showTransactions(List.of(), 0));
        assertEquals("Hasil Pencarian: \"kopi\"\n- Transaksi tidak ditemukan!",
                presenter.showSearchResults(List.of(), "kopi"));
        assertEquals("Hasil Pencarian: \"gaji\"\n[1] Gaji | Rp 5,000,000.00 | [Pemasukan]",
                presenter.showSearchResults(List.of(salary), "gaji"));
        assertEquals("Daftar Transaksi (Terurut):\n[2] Makan | Rp 25,000.00 | [Pengeluaran]",
                presenter.showSortedTransactions(List.of(food)));
    }

    @Test
    void formatsMessages() {
        assertEquals("Berhasil menambah pemasukan: [1] Gaji | Rp 5,000,000.00 | [Pemasukan]",
                presenter.showAddSuccess(salary));
        assertEquals("Berhasil menambah pengeluaran: [2] Makan | Rp 25,000.00 | [Pengeluaran]",
                presenter.showAddSuccess(food));
        assertEquals("Berhasil menghapus transaksi.", presenter.showRemoveSuccess());
        assertEquals("[!] Gagal menghapus transaksi dengan ID: 9.", presenter.showRemoveFailed(9));
        assertEquals("Berhasil mengubah transaksi.", presenter.showUpdateSuccess());
        assertEquals("[!] Gagal mengubah transaksi dengan ID: 9.", presenter.showUpdateFailed(9));
        assertEquals("[!] ID tidak valid!", presenter.showInvalidId());
        assertEquals("[!] Jumlah harus berupa angka", presenter.showValidationError("Jumlah harus berupa angka"));
    }
}
