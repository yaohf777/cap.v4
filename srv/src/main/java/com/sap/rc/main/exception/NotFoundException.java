package com.sap.rc.main.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// need to define exceptions with response status, not predefined
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends ReadinessCheckException {
    private static final long serialVersionUID = 1L;

    /**
     * @param message the reason for the exception
     */
    public NotFoundException(String message) {
        super(message);
    }

    /**
     * @param cause the underlying Throwable that caused this exception to be thrown.
     */
    public NotFoundException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message the reason for the exception
     * @param cause   the underlying Throwable that caused this exception to be thrown.
     */
    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
