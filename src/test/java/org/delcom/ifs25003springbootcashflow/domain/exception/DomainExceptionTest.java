package org.delcom.ifs25003springbootcashflow.domain.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

class DomainExceptionTest {

    @Test
    void messageOnlyConstructor() {
        assertEquals("pesan", new DomainException("pesan").getMessage());
    }

    @Test
    void messageAndCauseConstructor() {
        RuntimeException cause = new RuntimeException("akar");
        DomainException exception = new DomainException("pesan", cause);
        assertEquals("pesan", exception.getMessage());
        assertSame(cause, exception.getCause());
    }

    @Test
    void entityNotFoundKeepsIdAndMessage() {
        EntityNotFoundException generic = new EntityNotFoundException(42);
        assertEquals(42, generic.getId());
        assertEquals("Data dengan ID 42 tidak ditemukan.", generic.getMessage());

        EntityNotFoundException named = new EntityNotFoundException("Transaksi", 7);
        assertEquals(7, named.getId());
        assertEquals("Transaksi dengan ID 7 tidak ditemukan.", named.getMessage());
        assertInstanceOf(DomainException.class, named);
    }

    @Test
    void validationExceptionKeepsMessage() {
        ValidationException exception = new ValidationException("keterangan kosong");
        assertEquals("keterangan kosong", exception.getMessage());
        assertInstanceOf(DomainException.class, exception);
    }
}
