package com.enterprise.payment.service;

import com.enterprise.payment.entity.AuditLog;
import com.enterprise.payment.repository.AuditLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Base service class providing common functionality for all services
 * including audit logging, user context, and utility methods
 */
@Slf4j
public abstract class BaseService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    /**
     * Get the current authenticated user's username
     */
    protected String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "system";
    }

    /**
     * Create audit log entry
     */
    @Transactional
    protected void auditLog(String action, String entityType, Long entityId, 
                          String details, Map<String, Object> metadata) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setAction(action);
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setNewValues(Map.of("details", details));
            if (metadata != null) {
                auditLog.setOldValues(metadata);
            }
            
            auditLogRepository.save(auditLog);
            log.debug("Audit log created: {} - {} - {}", action, entityType, entityId);
        } catch (Exception e) {
            log.error("Failed to create audit log: {} - {} - {}", action, entityType, entityId, e);
            // Don't throw exception as audit logging shouldn't break business operations
        }
    }

    /**
     * Create audit log entry with simplified parameters
     */
    protected void auditLog(String action, String entityType, Long entityId, String details) {
        auditLog(action, entityType, entityId, details, null);
    }

    /**
     * Create pageable object with default sorting
     */
    protected Pageable createPageable(int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Sort sort = Sort.by(direction, sortBy);
        return PageRequest.of(page, size, sort);
    }

    /**
     * Create pageable object with default parameters
     */
    protected Pageable createPageable(int page, int size) {
        return createPageable(page, size, "createdAt", "DESC");
    }

    /**
     * Validate required string parameter
     */
    protected void validateRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }

    /**
     * Validate required object parameter
     */
    protected void validateRequired(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }

    /**
     * Log method entry with parameters
     */
    protected void logMethodEntry(String methodName, Object... params) {
        if (log.isDebugEnabled()) {
            log.debug("Entering {}.{} with params: {}", 
                     this.getClass().getSimpleName(), methodName, params);
        }
    }

    /**
     * Log method exit with result
     */
    protected void logMethodExit(String methodName, Object result) {
        if (log.isDebugEnabled()) {
            log.debug("Exiting {}.{} with result: {}", 
                     this.getClass().getSimpleName(), methodName, result);
        }
    }

    /**
     * Log method exit without result
     */
    protected void logMethodExit(String methodName) {
        if (log.isDebugEnabled()) {
            log.debug("Exiting {}.{}", this.getClass().getSimpleName(), methodName);
        }
    }
}