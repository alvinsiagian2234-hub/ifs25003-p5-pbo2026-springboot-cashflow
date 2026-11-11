package org.delcom.ifs25003springbootcashflow.domain.entity;

import java.util.List;

/**
 * Ringkasan keuangan: jumlah transaksi, total pemasukan, total pengeluaran, dan saldo.
 *
 * @param totalTransactions jumlah transaksi
 * @param totalIncome       total seluruh pemasukan
 * @param totalExpense      total seluruh pengeluaran
 */
public record BalanceSummary(int totalTransactions, long totalIncome, long totalExpense) {

    /** Saldo = total pemasukan - total pengeluaran. */
    public long balance() {
        return totalIncome - totalExpense;
    }

    /** Menghitung ringkasan dari daftar transaksi. */
    public static BalanceSummary of(List<Transaction> transactions) {
        long income = 0;
        long expense = 0;
        for (Transaction transaction : transactions) {
            if (transaction.isIncome()) {
                income += transaction.getAmount();
            } else {
                expense += transaction.getAmount();
            }
        }
        return new BalanceSummary(transactions.size(), income, expense);
    }
}
