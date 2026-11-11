package org.delcom.ifs25003springbootcashflow.domain.entity;

import java.util.Comparator;

/**
 * Kriteria pengurutan transaksi.
 * Setiap opsi membawa comparator-nya sendiri sehingga logika pengurutan
 * terpusat di domain, bukan tersebar di view / use case.
 */
public enum SortOption {
    /** Jumlah terkecil lebih dulu. */
    AMOUNT_ASC("Jumlah (terkecil)", Comparator.comparingLong(Transaction::getAmount)),

    /** Jumlah terbesar lebih dulu. */
    AMOUNT_DESC("Jumlah (terbesar)", Comparator.comparingLong(Transaction::getAmount).reversed()),

    /** Pemasukan ditampilkan lebih dulu. */
    INCOME_FIRST("Pemasukan dulu", Comparator.comparing(Transaction::isIncome).reversed()),

    /** Pengeluaran ditampilkan lebih dulu. */
    EXPENSE_FIRST("Pengeluaran dulu", Comparator.comparing(Transaction::isIncome)),

    /** Transaksi terbaru lebih dulu. */
    NEWEST_FIRST("Terbaru", Comparator.comparing(Transaction::getCreatedAt)
            .thenComparingInt(Transaction::getId).reversed()),

    /** Transaksi terlama lebih dulu. */
    OLDEST_FIRST("Terlama", Comparator.comparing(Transaction::getCreatedAt)
            .thenComparingInt(Transaction::getId)),

    /** Keterangan A-Z (case-insensitive). */
    DESCRIPTION_ASC("Keterangan (A-Z)",
            Comparator.comparing(Transaction::getDescription, String.CASE_INSENSITIVE_ORDER));

    private final String label;
    private final Comparator<Transaction> comparator;

    SortOption(String label, Comparator<Transaction> comparator) {
        this.label = label;
        this.comparator = comparator;
    }

    /** Label untuk ditampilkan di UI. */
    public String label() {
        return label;
    }

    /** Comparator yang dipakai untuk mengurutkan daftar transaksi. */
    public Comparator<Transaction> comparator() {
        return comparator;
    }
}
