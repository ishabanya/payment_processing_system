package com.enterprise.payment.controller;

import com.enterprise.payment.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Base controller class providing common functionality for all REST controllers.
 * Includes utilities for response handling, correlation IDs, pagination, and logging.
 */
@Slf4j
public abstract class BaseController {

    protected static final String REQUEST_ID_HEADER = "X-Request-ID";
    protected static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    protected static final String USER_ID_HEADER = "X-User-ID";

    /**
     * Creates a successful response with data
     */
    protected <T> ResponseEntity<ApiResponse<T>> success(T data) {
        return success(data, null);
    }

    /**
     * Creates a successful response with data and message
     */
    protected <T> ResponseEntity<ApiResponse<T>> success(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>(data, message);
        enrichResponse(response);
        logResponse("SUCCESS", response);
        return ResponseEntity.ok(response);
    }

    /**
     * Creates a successful response with data, message, and metadata
     */
    protected <T> ResponseEntity<ApiResponse<T>> success(T data, String message, ApiResponse.ResponseMetadata metadata) {
        ApiResponse<T> response = ApiResponse.success(data, message, metadata);
        enrichResponse(response);
        logResponse("SUCCESS", response);
        return ResponseEntity.ok(response);
    }

    /**
     * Creates a paginated response
     */
    protected <T> ResponseEntity<ApiResponse<T>> successWithPagination(Page<T> page, Pageable pageable) {
        return successWithPagination(page, pageable, null);
    }

    /**
     * Creates a paginated response with message
     */
    protected <T> ResponseEntity<ApiResponse<T>> successWithPagination(Page<T> page, Pageable pageable, String message) {
        ApiResponse.PaginationInfo paginationInfo = new ApiResponse.PaginationInfo(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious()
        );

        ApiResponse.SortingInfo sortingInfo = null;
        if (pageable.getSort().isSorted()) {
            sortingInfo = new ApiResponse.SortingInfo(
                    pageable.getSort().toString(),
                    pageable.getSort().iterator().next().getDirection().name(),
                    pageable.getSort().stream()
                            .map(order -> order.getProperty())
                            .toList()
            );
        }

        ApiResponse.ResponseMetadata metadata = new ApiResponse.ResponseMetadata();
        metadata.setPagination(paginationInfo);
        metadata.setSorting(sortingInfo);

        @SuppressWarnings("unchecked")
        T content = (T) page.getContent();
        return success(content, message, metadata);
    }

    /**
     * Creates an error response
     */
    protected <T> ResponseEntity<ApiResponse<T>> error(String message, HttpStatus status) {
        ApiResponse<T> response = ApiResponse.error(message);
        enrichResponse(response);
        logResponse("ERROR", response);
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Creates a bad request error response
     */
    protected <T> ResponseEntity<ApiResponse<T>> badRequest(String message) {
        return error(message, HttpStatus.BAD_REQUEST);
    }

    /**
     * Creates a not found error response
     */
    protected <T> ResponseEntity<ApiResponse<T>> notFound(String message) {
        return error(message, HttpStatus.NOT_FOUND);
    }

    /**
     * Creates an unauthorized error response
     */
    protected <T> ResponseEntity<ApiResponse<T>> unauthorized(String message) {
        return error(message, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Creates a forbidden error response
     */
    protected <T> ResponseEntity<ApiResponse<T>> forbidden(String message) {
        return error(message, HttpStatus.FORBIDDEN);
    }

    /**
     * Creates a conflict error response
     */
    protected <T> ResponseEntity<ApiResponse<T>> conflict(String message) {
        return error(message, HttpStatus.CONFLICT);
    }

    /**
     * Creates an internal server error response
     */
    protected <T> ResponseEntity<ApiResponse<T>> internalServerError(String message) {
        return error(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Creates a created response for POST operations
     */
    protected <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>(data, message);
        enrichResponse(response);
        logResponse("CREATED", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Creates an accepted response for async operations
     */
    protected <T> ResponseEntity<ApiResponse<T>> accepted(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>(data, message);
        enrichResponse(response);
        logResponse("ACCEPTED", response);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * Creates a no content response for DELETE operations
     */
    protected ResponseEntity<Void> noContent() {
        log.info("Returning NO_CONTENT response");
        return ResponseEntity.noContent().build();
    }

    /**
     * Gets the current correlation ID from MDC or generates a new one
     */
    protected String getCorrelationId() {
        String correlationId = MDC.get("correlationId");
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
            MDC.put("correlationId", correlationId);
        }
        return correlationId;
    }

    /**
     * Gets the current user ID from the request context
     */
    protected String getCurrentUserId() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            return request.getHeader(USER_ID_HEADER);
        }
        return null;
    }

    /**
     * Gets the current request
     */
    protected HttpServletRequest getCurrentRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) requestAttributes).getRequest();
        }
        return null;
    }

    /**
     * Enriches the response with correlation ID and other metadata
     */
    private <T> void enrichResponse(ApiResponse<T> response) {
        response.setRequestId(getCorrelationId());
        response.setTimestamp(OffsetDateTime.now());

        // Add environment and version info if available
        if (response.getMetadata() == null) {
            response.setMetadata(new ApiResponse.ResponseMetadata());
        }
        response.getMetadata().setVersion("1.0.0");
        response.getMetadata().setEnvironment(getEnvironment());
    }

    /**
     * Gets the current environment
     */
    private String getEnvironment() {
        return System.getProperty("spring.profiles.active", "development");
    }

    /**
     * Logs the response for monitoring and debugging
     */
    private <T> void logResponse(String type, ApiResponse<T> response) {
        String userId = getCurrentUserId();
        log.info("Response [{}] - RequestId: {}, UserId: {}, Success: {}, Message: {}",
                type, response.getRequestId(), userId, response.getSuccess(), response.getMessage());
    }

    /**
     * Common OpenAPI responses for controllers
     */
    public static class CommonApiResponses {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request",
                content = @Content(schema = @Schema(implementation = ApiResponse.class)))
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ApiResponse.class)))
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
                content = @Content(schema = @Schema(implementation = ApiResponse.class)))
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not Found",
                content = @Content(schema = @Schema(implementation = ApiResponse.class)))
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal Server Error",
                content = @Content(schema = @Schema(implementation = ApiResponse.class)))
        public @interface StandardErrorResponses {}
    }

    /**
     * Builds filters map from request parameters
     */
    protected Map<String, Object> buildFilters(HttpServletRequest request) {
        Map<String, Object> filters = new java.util.HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (!key.equals("page") && !key.equals("size") && !key.equals("sort")) {
                if (values.length == 1) {
                    filters.put(key, values[0]);
                } else {
                    filters.put(key, values);
                }
            }
        });
        return filters;
    }

    /**
     * Sets filters in response metadata
     */
    protected <T> void setFilters(ApiResponse<T> response, Map<String, Object> filters) {
        if (!filters.isEmpty()) {
            if (response.getMetadata() == null) {
                response.setMetadata(new ApiResponse.ResponseMetadata());
            }
            response.getMetadata().setFilters(filters);
        }
    }
}