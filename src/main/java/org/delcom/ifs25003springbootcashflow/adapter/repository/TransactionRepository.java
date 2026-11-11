package org.delcom.ifs25003springbootcashflow.adapter.repository;

import org.delcom.ifs25003springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25003springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25003springbootcashflow.domain.repository.ITransactionRepository;

import java.util.List;

/**
 * Implementasi in-memory repository transaksi (unit test).
 * Runtime memakai {@link JdbcTransactionRepository}.
 */
public class TransactionRepository extends InMemoryRepository<Transaction, Integer>
        implements ITransactionRepository {
    private int idCounter = 0;

    public TransactionRepository() {
        super(Transaction::getId);
    }

    @Override
    public Transaction save(int userId, String description, long amount, TransactionType type) {
        return save(new Transaction(nextId(), userId, description, amount, type));
    }

    @Override
    public List<Transaction> findByUserId(int userId) {
        return findBy(transaction -> transaction.getUserId() == userId);
    }

    private int nextId() {
        return ++idCounter;
    }
}
