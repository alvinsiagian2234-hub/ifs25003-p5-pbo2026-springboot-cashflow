package org.delcom.ifs25003springbootcashflow.domain.exception;

/**
 * Exception yang dilempar ketika entitas dengan ID tertentu tidak ditemukan.
 */
public class EntityNotFoundException extends DomainException {
    private final int id;

    public EntityNotFoundException(int id) {
        this("Data", id);
    }

    public EntityNotFoundException(String entityName, int id) {
        super(entityName + " dengan ID " + id + " tidak ditemukan.");
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
