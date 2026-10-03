package dev.learning.recordings;

import com.fasterxml.jackson.databind.JsonNode;

public final class InfraiRejectedException extends RuntimeException {
    private final int status;
    private final JsonNode error;

    public InfraiRejectedException(int status, JsonNode error) {
        super(error.path("message").asText("Infrai rejected the request"));
        this.status = status;
        this.error = error;
    }

    public int status() { return status; }
    public JsonNode error() { return error; }
}
