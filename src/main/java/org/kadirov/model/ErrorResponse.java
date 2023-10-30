package org.kadirov.model;

public record ErrorResponse(
        int statusCode,
        String message
) {
}
