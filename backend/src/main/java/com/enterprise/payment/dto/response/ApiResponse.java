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
public class ApiResponse<T> {

    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private T data;

    @JsonProperty("errors")
    private List<ErrorDetail> errors;

    @JsonProperty("metadata")
    private ResponseMetadata metadata;

    @JsonProperty("timestamp")
    private OffsetDateTime timestamp;

    @JsonProperty("requestId")
    private String requestId;

    // Constructor for successful responses
    public ApiResponse(T data) {
        this.success = true;
        this.data = data;
        this.timestamp = OffsetDateTime.now();
    }

    // Constructor for successful responses with message
    public ApiResponse(T data, String message) {
        this.success = true;
        this.data = data;
        this.message = message;
        this.timestamp = OffsetDateTime.now();
    }

    // Constructor for error responses
    public ApiResponse(List<ErrorDetail> errors) {
        this.success = false;
        this.errors = errors;
        this.timestamp = OffsetDateTime.now();
    }

    // Constructor for error responses with message
    public ApiResponse(String message, List<ErrorDetail> errors) {
        this.success = false;
        this.message = message;
        this.errors = errors;
        this.timestamp = OffsetDateTime.now();
    }

    // Static factory methods for common responses
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(data, message);
    }

    public static <T> ApiResponse<T> success(T data, String message, ResponseMetadata metadata) {
        ApiResponse<T> response = new ApiResponse<>(data, message);
        response.setMetadata(metadata);
        return response;
    }

    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setTimestamp(OffsetDateTime.now());
        return response;
    }

    public static <T> ApiResponse<T> error(String message, List<ErrorDetail> errors) {
        return new ApiResponse<>(message, errors);
    }

    public static <T> ApiResponse<T> error(List<ErrorDetail> errors) {
        return new ApiResponse<>(errors);
    }

    // Add error to existing response
    public ApiResponse<T> addError(ErrorDetail error) {
        if (this.errors == null) {
            this.errors = new java.util.ArrayList<>();
        }
        this.errors.add(error);
        this.success = false;
        return this;
    }

    // Set metadata
    public ApiResponse<T> withMetadata(ResponseMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

    // Set request ID
    public ApiResponse<T> withRequestId(String requestId) {
        this.requestId = requestId;
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

        // Constructor for field-specific errors
        public ErrorDetail(String field, String message) {
            this.field = field;
            this.message = message;
        }

        // Constructor for general errors
        public ErrorDetail(String message) {
            this.message = message;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ResponseMetadata {
        @JsonProperty("pagination")
        private PaginationInfo pagination;

        @JsonProperty("sorting")
        private SortingInfo sorting;

        @JsonProperty("filters")
        private Map<String, Object> filters;

        @JsonProperty("executionTime")
        private Long executionTimeMs;

        @JsonProperty("version")
        private String version;

        @JsonProperty("environment")
        private String environment;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PaginationInfo {
        @JsonProperty("page")
        private Integer page;

        @JsonProperty("size")
        private Integer size;

        @JsonProperty("totalElements")
        private Long totalElements;

        @JsonProperty("totalPages")
        private Integer totalPages;

        @JsonProperty("hasNext")
        private Boolean hasNext;

        @JsonProperty("hasPrevious")
        private Boolean hasPrevious;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SortingInfo {
        @JsonProperty("sortBy")
        private String sortBy;

        @JsonProperty("sortDirection")
        private String sortDirection;

        @JsonProperty("sortFields")
        private List<String> sortFields;
    }
}