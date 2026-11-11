package org.delcom.ifs25003springbootcashflow.adapter.presenter;

import org.delcom.ifs25003springbootcashflow.domain.entity.BalanceSummary;
import org.delcom.ifs25003springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25003springbootcashflow.domain.entity.TransactionType;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Presenter yang memformat data keuangan menjadi teks siap tampil.
 * Format uang: {@code Rp 1,000,000.00} (pemisah ribuan koma, dua digit desimal).
 * Entity dan use case tidak mengurus format tampilan.
 */
public class FinancePresenter {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Memformat jumlah uang, mis. 1000000 menjadi "Rp 1,000,000.00". */
    public String formatMoney(long amount) {
        DecimalFormat format = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        return "Rp " + format.format(amount);
    }

    /** Memformat jumlah bertanda: "+Rp 1,000.00" untuk pemasukan, "-Rp 1,000.00" untuk pengeluaran. */
    public String formatSignedAmount(Transaction transaction) {
        String prefix = transaction.isIncome() ? "+" : "-";
        return prefix + formatMoney(transaction.getAmount());
    }

    /** Label jenis transaksi: Pemasukan / Pengeluaran. */
    public String formatType(TransactionType type) {
        return type.label();
    }

    /** Memformat waktu transaksi. */
    public String formatCreatedAt(Transaction transaction) {
        return transaction.getCreatedAt().format(DATE_TIME_FORMAT);
    }

    /** Memformat satu transaksi: "[id] keterangan | Rp 1,000,000.00 | [Pemasukan]". */
    public String format(Transaction transaction) {
        return String.format(
                "[%d] %s | %s | [%s]",
                transaction.getId(),
                transaction.getDescription(),
                formatMoney(transaction.getAmount()),
                formatType(transaction.getType())
        );
    }

    /** Memformat saldo: "Saldo: Rp 4,975,000.00". */
    public String formatBalance(long balance) {
        return "Saldo: " + formatMoney(balance);
    }

    /** Memformat ringkasan multi-baris (dipakai di notifikasi / laporan teks). */
    public String formatSummary(BalanceSummary summary) {
        return String.join("\n",
                "Jumlah Transaksi: " + summary.totalTransactions(),
                "Total Pemasukan: " + formatMoney(summary.totalIncome()),
                "Total Pengeluaran: " + formatMoney(summary.totalExpense()),
                formatBalance(summary.balance()));
    }

    /** Memformat daftar transaksi + saldo menjadi teks multi-baris. */
    public String formatList(List<Transaction> transactions, String header, String emptyMessage) {
        StringBuilder builder = new StringBuilder(header);
        if (transactions.isEmpty()) {
            builder.append('\n').append(emptyMessage);
            return builder.toString();
        }
        String body = transactions.stream()
                .map(this::format)
                .collect(Collectors.joining("\n"));
        builder.append('\n').append(body);
        return builder.toString();
    }

    public String showTransactions(List<Transaction> transactions, long balance) {
        return formatList(transactions, "Daftar Transaksi:", "- Data transaksi belum tersedia!")
                + "\n" + formatBalance(balance);
    }

    public String showSearchResults(List<Transaction> transactions, String keyword) {
        return formatList(transactions, "Hasil Pencarian: \"" + keyword + "\"", "- Transaksi tidak ditemukan!");
    }

    public String showSortedTransactions(List<Transaction> transactions) {
        return formatList(transactions, "Daftar Transaksi (Terurut):", "- Data transaksi belum tersedia!");
    }

    public String showAddSuccess(Transaction transaction) {
        return "Berhasil menambah " + formatType(transaction.getType()).toLowerCase(Locale.ROOT)
                + ": " + format(transaction);
    }

    public String showRemoveSuccess() {
        return "Berhasil menghapus transaksi.";
    }

    public String showRemoveFailed(int id) {
        return String.format("[!] Gagal menghapus transaksi dengan ID: %d.", id);
    }

    public String showUpdateSuccess() {
        return "Berhasil mengubah transaksi.";
    }

    public String showUpdateFailed(int id) {
        return String.format("[!] Gagal mengubah transaksi dengan ID: %d.", id);
    }

    public String showInvalidId() {
        return "[!] ID tidak valid!";
    }

    public String showValidationError(String message) {
        return "[!] " + message;
    }
}
