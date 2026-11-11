package org.delcom.ifs25003springbootcashflow.adapter.repository;

import org.delcom.ifs25003springbootcashflow.domain.repository.IRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Base generic in-memory repository yang mengimplementasikan {@link IRepository}.
 * Dipakai untuk unit test (runtime memakai implementasi JDBC).
 *
 * @param <T>  tipe entitas domain
 * @param <ID> tipe identifier entitas
 */
public abstract class InMemoryRepository<T, ID> implements IRepository<T, ID> {
    /** Penyimpanan data entitas di memori. */
    protected final List<T> data = new ArrayList<>();

    /** Fungsi untuk mengekstrak ID dari entitas. */
    private final Function<T, ID> idExtractor;

    protected InMemoryRepository(Function<T, ID> idExtractor) {
        this.idExtractor = idExtractor;
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(data);
    }

    @Override
    public Optional<T> findById(ID id) {
        return data.stream()
                .filter(item -> idExtractor.apply(item).equals(id))
                .findFirst();
    }

    @Override
    public T save(T entity) {
        data.add(entity);
        return entity;
    }

    @Override
    public boolean deleteById(ID id) {
        return data.removeIf(item -> idExtractor.apply(item).equals(id));
    }

    @Override
    public void update(T entity) {
        // Objek mutable disimpan by-reference di memori; tidak perlu aksi tambahan.
    }

    @Override
    public List<T> findBy(Predicate<T> predicate) {
        return data.stream()
                .filter(predicate)
                .toList();
    }
}
