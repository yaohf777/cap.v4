package com.sap.rc.main.exception;

/**
 * Root exception for all Readiness Check runtime exceptions. This class is used as the root instead of
 * {@link java.lang.SecurityException} to remove the potential for conflicts; many other frameworks and products (such
 * as J2EE containers) perform special operations when encountering {@link java.lang.SecurityException}.
 *
 */
public class ReadinessCheckException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    

    /**
     * @param message the reason for the exception
     */
    public ReadinessCheckException(String message) {
        super(message);
    }

    /**
     * @param cause the underlying Throwable that caused this exception to be thrown.
     */
    public ReadinessCheckException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message the reason for the exception
     * @param cause   the underlying Throwable that caused this exception to be thrown.
     */
    public ReadinessCheckException(String message, Throwable cause) {
        super(message, cause);
    }

}
