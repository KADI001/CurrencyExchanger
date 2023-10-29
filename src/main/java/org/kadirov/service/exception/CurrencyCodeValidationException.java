package org.kadirov.service.exception;

public class CurrencyCodeValidationException extends Exception {
    public CurrencyCodeValidationException() {
    }

    public CurrencyCodeValidationException(String message) {
        super(message);
    }

    public CurrencyCodeValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public CurrencyCodeValidationException(Throwable cause) {
        super(cause);
    }

    public CurrencyCodeValidationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
