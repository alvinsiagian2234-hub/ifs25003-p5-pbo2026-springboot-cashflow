package org.delcom.ifs25003springbootcashflow.domain.exception;

/**
 * Checked exception dasar untuk hierarki domain aplikasi Cashflow.
 */
public class DomainException extends Exception {
    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
