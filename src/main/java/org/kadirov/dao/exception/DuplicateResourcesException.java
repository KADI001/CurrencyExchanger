package org.kadirov.dao.exception;

public class DuplicateResourcesException extends RuntimeException{
    public DuplicateResourcesException() {
    }

    public DuplicateResourcesException(String message) {
        super(message);
    }

    public DuplicateResourcesException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateResourcesException(Throwable cause) {
        super(cause);
    }

    public DuplicateResourcesException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
