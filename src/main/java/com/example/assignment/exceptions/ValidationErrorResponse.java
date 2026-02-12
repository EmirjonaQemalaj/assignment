package com.example.assignment.exceptions;

import java.util.List;

public class ValidationErrorResponse {

    private String timestamp;

    private int status;

    private String message;

    private String error;

    private String path;

    private List<Violation> violations;

    private ValidationErrorResponse() {
    }

    public static class Builder {

        private ValidationErrorResponse response;

        private Builder() {
            response = new ValidationErrorResponse();
        }

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder atTimestamp(String timestamp) {
            response.setTimestamp(timestamp);
            return this;
        }

        public Builder withStatus(int status) {
            response.setStatus(status);
            return this;
        }

        public Builder withError(String error) {
            response.setError(error);
            return this;
        }

        public Builder withMessage(String message) {
            response.setMessage(message);
            return this;
        }

        public Builder atPath(String path) {
            response.setPath(path);
            return this;
        }

        public Builder withViolations(List<Violation> violations) {
            response.setViolations(violations);
            return this;
        }

        public ValidationErrorResponse build() {
            return response;
        }
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<Violation> getViolations() {
        return violations;
    }

    public void setViolations(List<Violation> violations) {
        this.violations = violations;
    }
}
