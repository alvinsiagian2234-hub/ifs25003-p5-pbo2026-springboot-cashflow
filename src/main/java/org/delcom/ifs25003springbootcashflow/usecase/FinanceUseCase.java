package org.delcom.ifs25003springbootcashflow.usecase;

import org.delcom.ifs25003springbootcashflow.domain.entity.BalanceSummary;
import org.delcom.ifs25003springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25003springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25003springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25003springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25003springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25003springbootcashflow.domain.repository.ITransactionRepository;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Use case logika bisnis keuangan (cashflow).
 * Tidak melakukan I/O UI; hanya memproses data dan melempar checked exception
 * domain jika input tidak valid atau data tidak ditemukan. Semua operasi
 * tulis/baca dibatasi (scoped) per pengguna.
 */
public class FinanceUseCase {
    /** Angka polos, mis. 1000000. */
    private static final Pattern PLAIN_NUMBER = Pattern.compile("\\d+");

    /** Angka dengan pemisah ribuan titik atau koma, mis. 1.000.000 atau 1,000,000. */
    private static final Pattern GROUPED_NUMBER = Pattern.compile("\\d{1,3}([.,]\\d{3})+");

    private final ITransactionRepository transactionRepository;

    public FinanceUseCase(ITransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /** Mengambil semua transaksi milik satu pengguna. */
    public List<Transaction> getTransactionsByUser(int userId) {
        return transactionRepository.findByUserId(userId);
    }

    /** Mengambil semua transaksi di sistem (keperluan admin). */
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    /** Menambah pemasukan dari input teks jumlah. */
    public Transaction addIncome(int userId, String description, String amountText) throws ValidationException {
        return addTransaction(userId, description, parseAmount(amountText), TransactionType.INCOME);
    }

    /** Menambah pengeluaran dari input teks jumlah. */
    public Transaction addExpense(int userId, String description, String amountText) throws ValidationException {
        return addTransaction(userId, description, parseAmount(amountText), TransactionType.EXPENSE);
    }

    /** Menambah transaksi baru setelah validasi. */
    public Transaction addTransaction(int userId, String description, long amount, TransactionType type)
            throws ValidationException {
        String safeDescription = requireDescription(description);
        requirePositive(amount);
        requireType(type);
        return transactionRepository.save(userId, safeDescription, amount, type);
    }

    /** Menghapus transaksi berdasarkan ID; melempar exception jika tidak ditemukan. */
    public void removeTransaction(int id) throws EntityNotFoundException, ValidationException {
        requireValidId(id);
        if (!transactionRepository.deleteById(id)) {
            throw new EntityNotFoundException("Transaksi", id);
        }
    }

    /** Menghapus transaksi milik pengguna tertentu saja. */
    public void removeTransactionForUser(int userId, int id) throws EntityNotFoundException, ValidationException {
        Transaction transaction = requireOwnedTransaction(userId, id);
        removeTransaction(transaction.getId());
    }

    /**
     * Mengubah transaksi milik pengguna.
     * Parameter {@code null} berarti field tersebut tidak diubah
     * (kecuali {@code clearProof=true} untuk menghapus bukti).
     */
    public Transaction updateTransactionForUser(
            int userId,
            int id,
            String description,
            String amountText,
            TransactionType type,
            String proofPath,
            boolean clearProof
    ) throws EntityNotFoundException, ValidationException {
        Transaction transaction = requireOwnedTransaction(userId, id);
        String newDescription = description == null ? null : requireDescription(description);
        Long newAmount = amountText == null ? null : parseAmount(amountText);

        if (newDescription != null) {
            transaction.changeDescription(newDescription);
        }
        if (newAmount != null) {
            transaction.changeAmount(newAmount);
        }
        if (type != null) {
            transaction.changeType(type);
        }
        if (clearProof) {
            transaction.changeProofPath(null);
        } else if (proofPath != null) {
            transaction.changeProofPath(proofPath);
        }
        transactionRepository.update(transaction);
        return transaction;
    }

    /** Mengambil satu transaksi milik pengguna. */
    public Transaction getTransactionForUser(int userId, int id) throws EntityNotFoundException, ValidationException {
        return requireOwnedTransaction(userId, id);
    }

    /** Mencari transaksi pengguna yang keterangannya mengandung kata kunci (case-insensitive). */
    public List<Transaction> searchTransactions(int userId, String keyword) {
        List<Transaction> owned = transactionRepository.findByUserId(userId);
        if (keyword == null || keyword.isBlank()) {
            return owned;
        }
        String lowerKeyword = keyword.trim().toLowerCase(Locale.ROOT);
        return owned.stream()
                .filter(t -> t.getDescription().toLowerCase(Locale.ROOT).contains(lowerKeyword))
                .toList();
    }

    /** Mengurutkan transaksi pengguna sesuai {@link SortOption}. */
    public List<Transaction> sortTransactions(int userId, SortOption option) throws ValidationException {
        if (option == null) {
            throw new ValidationException("Opsi pengurutan tidak valid");
        }
        return transactionRepository.findByUserId(userId).stream()
                .sorted(option.comparator())
                .toList();
    }

    /**
     * Mencari (opsional) lalu mengurutkan (opsional) transaksi pengguna.
     * Keyword kosong berarti semua transaksi; option {@code null} berarti urutan ID.
     */
    public List<Transaction> findTransactions(int userId, String keyword, SortOption option) {
        List<Transaction> result = searchTransactions(userId, keyword);
        if (option == null) {
            return result;
        }
        return result.stream().sorted(option.comparator()).toList();
    }

    /** Saldo pengguna: total pemasukan - total pengeluaran. */
    public long getBalance(int userId) {
        return getSummary(userId).balance();
    }

    /** Ringkasan keuangan pengguna (jumlah transaksi, pemasukan, pengeluaran, saldo). */
    public BalanceSummary getSummary(int userId) {
        return BalanceSummary.of(transactionRepository.findByUserId(userId));
    }

    /**
     * Mengubah teks jumlah menjadi angka.
     * Menerima angka polos (1000000) atau berpemisah ribuan (1.000.000 / 1,000,000).
     */
    public long parseAmount(String amountText) throws ValidationException {
        if (amountText == null || amountText.isBlank()) {
            throw new ValidationException("Jumlah tidak boleh kosong");
        }
        String text = amountText.trim().replace(" ", "");
        if (text.startsWith("-") && isNumeric(text.substring(1))) {
            throw new ValidationException("Jumlah harus lebih dari 0");
        }
        if (!isNumeric(text)) {
            throw new ValidationException("Jumlah harus berupa angka");
        }
        long amount;
        try {
            amount = Long.parseLong(text.replace(".", "").replace(",", ""));
        } catch (NumberFormatException ex) {
            throw new ValidationException("Jumlah terlalu besar");
        }
        requirePositive(amount);
        return amount;
    }

    /** Mengubah teks ID menjadi angka positif. */
    public int parseId(String idText) throws ValidationException {
        if (idText == null || idText.isBlank()) {
            throw new ValidationException("ID tidak valid");
        }
        int id;
        try {
            id = Integer.parseInt(idText.trim());
        } catch (NumberFormatException ex) {
            throw new ValidationException("ID tidak valid");
        }
        requireValidId(id);
        return id;
    }

    private static boolean isNumeric(String text) {
        return PLAIN_NUMBER.matcher(text).matches() || GROUPED_NUMBER.matcher(text).matches();
    }

    private Transaction requireOwnedTransaction(int userId, int id)
            throws EntityNotFoundException, ValidationException {
        requireValidId(id);
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transaksi", id));
        if (transaction.getUserId() != userId) {
            throw new ValidationException("Transaksi tidak milik pengguna ini");
        }
        return transaction;
    }

    private static void requireValidId(int id) throws ValidationException {
        if (id <= 0) {
            throw new ValidationException("ID tidak valid");
        }
    }

    private static String requireDescription(String description) throws ValidationException {
        if (description == null || description.trim().isEmpty()) {
            throw new ValidationException("Keterangan tidak boleh kosong");
        }
        return description.trim();
    }

    private static void requirePositive(long amount) throws ValidationException {
        if (amount <= 0) {
            throw new ValidationException("Jumlah harus lebih dari 0");
        }
    }

    private static void requireType(TransactionType type) throws ValidationException {
        if (type == null) {
            throw new ValidationException("Jenis transaksi tidak boleh kosong");
        }
    }
}
