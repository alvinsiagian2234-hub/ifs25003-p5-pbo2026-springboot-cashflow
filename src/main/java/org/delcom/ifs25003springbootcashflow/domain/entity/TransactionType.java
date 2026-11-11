package org.delcom.ifs25003springbootcashflow.domain.entity;

/**
 * Jenis transaksi keuangan.
 * Pemasukan menambah saldo, pengeluaran mengurangi saldo.
 */
public enum TransactionType {
    /** Pemasukan: menambah saldo. */
    INCOME("Pemasukan", 1),

    /** Pengeluaran: mengurangi saldo. */
    EXPENSE("Pengeluaran", -1);

    private final String label;
    private final int sign;

    TransactionType(String label, int sign) {
        this.label = label;
        this.sign = sign;
    }

    /** Label bahasa Indonesia untuk jenis transaksi. */
    public String label() {
        return label;
    }

    /** Pengali saldo: +1 untuk pemasukan, -1 untuk pengeluaran. */
    public int sign() {
        return sign;
    }
}
