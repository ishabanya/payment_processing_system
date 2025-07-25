package com.enterprise.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    @JsonProperty("error")
    private Boolean error = true;

    @JsonProperty("errorCode")
    private String errorCode;

    @JsonProperty("message")
    private String message;

    @JsonProperty("details")
    private List<ErrorDetail> details;

    @JsonProperty("timestamp")
    private OffsetDateTime timestamp = OffsetDateTime.now();

    @JsonProperty("path")
    private String path;

    @JsonProperty("method")
    private String method;

    @JsonProperty("requestId")
    private String requestId;

    @JsonProperty("supportReference")
    private String supportReference;

    @JsonProperty("additionalInfo")
    private Map<String, Object> additionalInfo;

    // Constructor for simple error
    public ErrorResponse(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
        this.timestamp = OffsetDateTime.now();
    }

    // Constructor with details
    public ErrorResponse(String errorCode, String message, List<ErrorDetail> details) {
        this.errorCode = errorCode;
        this.message = message;
        this.details = details;
        this.timestamp = OffsetDateTime.now();
    }

    // Static factory methods for common error types
    public static ErrorResponse badRequest(String message) {
        return new ErrorResponse("BAD_REQUEST", message);
    }

    public static ErrorResponse badRequest(String message, List<ErrorDetail> details) {
        return new ErrorResponse("BAD_REQUEST", message, details);
    }

    public static ErrorResponse unauthorized(String message) {
        return new ErrorResponse("UNAUTHORIZED", message != null ? message : "Authentication required");
    }

    public static ErrorResponse forbidden(String message) {
        return new ErrorResponse("FORBIDDEN", message != null ? message : "Access denied");
    }

    public static ErrorResponse notFound(String resource) {
        return new ErrorResponse("NOT_FOUND", resource + " not found");
    }

    public static ErrorResponse conflict(String message) {
        return new ErrorResponse("CONFLICT", message);
    }

    public static ErrorResponse unprocessableEntity(String message, List<ErrorDetail> details) {
        return new ErrorResponse("UNPROCESSABLE_ENTITY", message, details);
    }

    public static ErrorResponse internalServerError(String message) {
        return new ErrorResponse("INTERNAL_SERVER_ERROR", 
                                message != null ? message : "An unexpected error occurred");
    }

    public static ErrorResponse serviceUnavailable(String message) {
        return new ErrorResponse("SERVICE_UNAVAILABLE", 
                                message != null ? message : "Service temporarily unavailable");
    }

    public static ErrorResponse rateLimitExceeded(String message) {
        return new ErrorResponse("RATE_LIMIT_EXCEEDED", 
                                message != null ? message : "Rate limit exceeded");
    }

    public static ErrorResponse paymentError(String message) {
        return new ErrorResponse("PAYMENT_ERROR", message);
    }

    public static ErrorResponse validationError(String message, List<ErrorDetail> details) {
        return new ErrorResponse("VALIDATION_ERROR", message, details);
    }

    // Builder methods
    public ErrorResponse withPath(String path) {
        this.path = path;
        return this;
    }

    public ErrorResponse withMethod(String method) {
        this.method = method;
        return this;
    }

    public ErrorResponse withRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public ErrorResponse withSupportReference(String supportReference) {
        this.supportReference = supportReference;
        return this;
    }

    public ErrorResponse withAdditionalInfo(Map<String, Object> additionalInfo) {
        this.additionalInfo = additionalInfo;
        return this;
    }

    public ErrorResponse withAdditionalInfo(String key, Object value) {
        if (this.additionalInfo == null) {
            this.additionalInfo = new java.util.HashMap<>();
        }
        this.additionalInfo.put(key, value);
        return this;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorDetail {
        @JsonProperty("field")
        private String field;

        @JsonProperty("code")
        private String code;

        @JsonProperty("message")
        private String message;

        @JsonProperty("rejectedValue")
        private Object rejectedValue;

        @JsonProperty("location")
        private String location; // e.g., "body", "query", "path"

        // Constructor for field-specific validation errors
        public ErrorDetail(String field, String code, String message, Object rejectedValue) {
            this.field = field;
            this.code = code;
            this.message = message;
            this.rejectedValue = rejectedValue;
        }

        // Constructor for simple field errors
        public ErrorDetail(String field, String message) {
            this.field = field;
            this.message = message;
        }

        // Constructor for general errors
        public ErrorDetail(String message) {
            this.message = message;
        }
    }
}