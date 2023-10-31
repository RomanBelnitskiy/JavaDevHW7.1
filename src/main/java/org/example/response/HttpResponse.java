package org.example.response;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HttpResponse {
    private String protocol;
    private int statusCode;
    private String statusMessage;
    private Map<String, String> headers = new HashMap<>();
    private String body;

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
        if (body != null && !body.isEmpty()) {
            addHeader("Content-Length", String.valueOf(body.getBytes(StandardCharsets.UTF_8).length));
        }
    }

    public Map<String, String> getHeaders() {
        return Map.copyOf(headers);
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public String getHeader(String key) {
        return headers.getOrDefault(key, "");
    }

    @Override
    public String toString() {
        return "HttpResponse{" +
                "protocol='" + protocol + '\'' +
                ", headers=" + headers +
                ", body='" + body + '\'' +
                '}';
    }

    public String convertToText() {
        StringBuilder out = new StringBuilder();
        out.append(protocol)
                .append(" ")
                .append(statusCode)
                .append(" ")
                .append(statusMessage)
                .append("\r\n");

        Set<Map.Entry<String, String>> headerEntries = headers.entrySet();
        for (Map.Entry<String, String> entry : headerEntries) {
            out.append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue())
                    .append("\r\n");
        }
        out.append("\r\n");

        if (body != null && !body.isEmpty()) {
            out.append(body);
        }

        return out.toString();
    }
}
