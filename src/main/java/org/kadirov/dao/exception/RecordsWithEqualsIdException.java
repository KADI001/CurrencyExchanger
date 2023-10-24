package org.kadirov.dao.exception;

public class RecordsWithEqualsIdException extends RuntimeException {
    public RecordsWithEqualsIdException() {
    }

    public RecordsWithEqualsIdException(String message) {
        super(message);
    }

    public RecordsWithEqualsIdException(String message, Throwable cause) {
        super(message, cause);
    }
}
