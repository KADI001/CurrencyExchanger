package org.kadirov.util;

import jakarta.servlet.http.HttpServletRequest;

import java.io.BufferedReader;
import java.io.IOException;

public final class HttpRequestUtil {
    private HttpRequestUtil(){}

    public static String extractBodyAsString(HttpServletRequest httpServletRequest) throws IOException {
        BufferedReader reader = httpServletRequest.getReader();
        StringBuilder body = new StringBuilder();
        reader.lines().forEach(body::append);
        return body.toString();
    }
}
