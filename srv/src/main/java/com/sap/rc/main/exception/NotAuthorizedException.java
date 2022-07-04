package com.sap.rc.main.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class NotAuthorizedException extends ReadinessCheckException {
    private static final long serialVersionUID = 1L;

    /**
     * @param message the reason for the exception
     */
    public NotAuthorizedException(String message) {
        super(message);
    }

    /**
     * @param cause the underlying Throwable that caused this exception to be thrown.
     */
    public NotAuthorizedException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message the reason for the exception
     * @param cause   the underlying Throwable that caused this exception to be thrown.
     */
    public NotAuthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
