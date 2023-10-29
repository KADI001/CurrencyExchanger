package org.kadirov.model;

public record ErrorModel(
        int statusCode,
        String message
) {
}
