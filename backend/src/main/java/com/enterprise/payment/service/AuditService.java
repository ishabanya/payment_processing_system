package com.enterprise.payment.service;

import com.enterprise.payment.entity.AuditLog;
import com.enterprise.payment.entity.User;
import com.enterprise.payment.repository.AuditLogRepository;
import com.enterprise.payment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for audit logging and compliance tracking
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService extends BaseService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    /**
     * Create comprehensive audit log entry
     */
    @Transactional
    public AuditLog createAuditLog(String action, String entityType, Long entityId,
                                  Map<String, Object> oldValues, Map<String, Object> newValues,
                                  String correlationId, String username, String ipAddress, String userAgent) {
        logMethodEntry("createAuditLog", action, entityType, entityId);
        
        User user = null;
        if (username != null) {
            user = userRepository.findByUsername(username).orElse(null);
        }
        
        AuditLog auditLog = AuditLog.create(
            entityType, entityId, action, oldValues, newValues,
            correlationId, user, parseIpAddress(ipAddress), userAgent
        );
        
        auditLog = auditLogRepository.save(auditLog);
        
        log.debug("Audit log created: {} - {} - {}", action, entityType, entityId);
        logMethodExit("createAuditLog", auditLog.getId());
        return auditLog;
    }

    /**
     * Create simplified audit log entry
     */
    @Transactional
    public AuditLog createAuditLog(String action, String entityType, Long entityId, String details) {
        logMethodEntry("createAuditLog", action, entityType, entityId, details);
        
        Map<String, Object> newValues = Map.of("details", details);
        String currentUser = getCurrentUsername();
        
        AuditLog auditLog = createAuditLog(action, entityType, entityId, null, newValues,
                                          null, currentUser, null, null);
        
        logMethodExit("createAuditLog", auditLog.getId());
        return auditLog;
    }

    /**
     * Get audit logs by entity
     */
    public Page<AuditLog> getAuditLogsByEntity(String entityType, Long entityId, Pageable pageable) {
        logMethodEntry("getAuditLogsByEntity", entityType, entityId, pageable);
        
        Page<AuditLog> auditLogs = auditLogRepository
            .findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId, pageable);
        
        logMethodExit("getAuditLogsByEntity", auditLogs.getTotalElements());
        return auditLogs;
    }

    /**
     * Get audit logs by user
     */
    public Page<AuditLog> getAuditLogsByUser(String username, Pageable pageable) {
        logMethodEntry("getAuditLogsByUser", username, pageable);
        
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return Page.empty(pageable);
        }
        
        Page<AuditLog> auditLogs = auditLogRepository
            .findByUserOrderByCreatedAtDesc(user, pageable);
        
        logMethodExit("getAuditLogsByUser", auditLogs.getTotalElements());
        return auditLogs;
    }

    /**
     * Get audit logs by action
     */
    public Page<AuditLog> getAuditLogsByAction(String action, Pageable pageable) {
        logMethodEntry("getAuditLogsByAction", action, pageable);
        
        Page<AuditLog> auditLogs = auditLogRepository
            .findByActionOrderByCreatedAtDesc(action, pageable);
        
        logMethodExit("getAuditLogsByAction", auditLogs.getTotalElements());
        return auditLogs;
    }

    /**
     * Get audit logs by date range
     */
    public Page<AuditLog> getAuditLogsByDateRange(OffsetDateTime startDate, OffsetDateTime endDate, 
                                                 Pageable pageable) {
        logMethodEntry("getAuditLogsByDateRange", startDate, endDate, pageable);
        
        Page<AuditLog> auditLogs = auditLogRepository
            .findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate, pageable);
        
        logMethodExit("getAuditLogsByDateRange", auditLogs.getTotalElements());
        return auditLogs;
    }

    /**
     * Get audit logs by correlation ID
     */
    public List<AuditLog> getAuditLogsByCorrelationId(String correlationId) {
        logMethodEntry("getAuditLogsByCorrelationId", correlationId);
        
        List<AuditLog> auditLogs = auditLogRepository
            .findByCorrelationIdOrderByCreatedAtAsc(correlationId);
        
        logMethodExit("getAuditLogsByCorrelationId", auditLogs.size());
        return auditLogs;
    }

    /**
     * Get audit statistics
     */
    public Map<String, Object> getAuditStatistics(OffsetDateTime startDate, OffsetDateTime endDate) {
        logMethodEntry("getAuditStatistics", startDate, endDate);
        
        Map<String, Object> stats = new HashMap<>();
        
        // Total audit logs in period
        Long totalLogs = auditLogRepository.countByCreatedAtBetween(startDate, endDate);
        stats.put("totalAuditLogs", totalLogs);
        
        // Most active users
        List<Object[]> activeUsers = auditLogRepository
            .findTopActiveUsersByDateRange(startDate, endDate);
        stats.put("mostActiveUsers", activeUsers);
        
        // Most common actions
        List<Object[]> commonActions = auditLogRepository
            .findTopActionsByDateRange(startDate, endDate);
        stats.put("mostCommonActions", commonActions);
        
        // Activity by entity type
        List<Object[]> entityActivity = auditLogRepository
            .findActivityByEntityType(startDate, endDate);
        stats.put("activityByEntityType", entityActivity);
        
        // Daily activity summary
        List<Object[]> dailyActivity = auditLogRepository
            .findDailyActivitySummary(startDate, endDate);
        stats.put("dailyActivity", dailyActivity);
        
        logMethodExit("getAuditStatistics", stats);
        return stats;
    }

    /**
     * Get user activity timeline
     */
    public List<AuditLog> getUserActivityTimeline(String username, OffsetDateTime startDate, 
                                                 OffsetDateTime endDate) {
        logMethodEntry("getUserActivityTimeline", username, startDate, endDate);
        
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return List.of();
        }
        
        List<AuditLog> timeline = auditLogRepository
            .findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(user, startDate, endDate);
        
        logMethodExit("getUserActivityTimeline", timeline.size());
        return timeline;
    }

    /**
     * Search audit logs
     */
    public Page<AuditLog> searchAuditLogs(String searchTerm, Pageable pageable) {
        logMethodEntry("searchAuditLogs", searchTerm, pageable);
        
        Page<AuditLog> auditLogs = auditLogRepository
            .findBySearchTerm(searchTerm, pageable);
        
        logMethodExit("searchAuditLogs", auditLogs.getTotalElements());
        return auditLogs;
    }

    /**
     * Generate compliance report
     */
    public Map<String, Object> generateComplianceReport(OffsetDateTime startDate, OffsetDateTime endDate) {
        logMethodEntry("generateComplianceReport", startDate, endDate);
        
        Map<String, Object> report = new HashMap<>();
        
        // Get audit statistics
        Map<String, Object> stats = getAuditStatistics(startDate, endDate);
        report.putAll(stats);
        
        // Security-related events
        List<AuditLog> securityEvents = auditLogRepository
            .findSecurityRelatedEvents(startDate, endDate);
        report.put("securityEvents", securityEvents);
        
        // Failed operations
        List<AuditLog> failedOperations = auditLogRepository
            .findFailedOperations(startDate, endDate);
        report.put("failedOperations", failedOperations);
        
        // High-risk activities
        List<AuditLog> highRiskActivities = auditLogRepository
            .findHighRiskActivities(startDate, endDate);
        report.put("highRiskActivities", highRiskActivities);
        
        // Data access patterns
        List<Object[]> dataAccessPatterns = auditLogRepository
            .findDataAccessPatterns(startDate, endDate);
        report.put("dataAccessPatterns", dataAccessPatterns);
        
        // Generate report metadata
        report.put("reportGeneratedAt", OffsetDateTime.now());
        report.put("reportPeriodStart", startDate);
        report.put("reportPeriodEnd", endDate);
        report.put("reportGeneratedBy", getCurrentUsername());
        
        // Create audit log for report generation
        auditLog("COMPLIANCE_REPORT_GENERATED", "AUDIT", null,
                String.format("Compliance report generated for period %s to %s", startDate, endDate));
        
        logMethodExit("generateComplianceReport", report);
        return report;
    }

    /**
     * Clean up old audit logs (for data retention compliance)
     */
    @Transactional
    public long cleanupOldAuditLogs(OffsetDateTime cutoffDate) {
        logMethodEntry("cleanupOldAuditLogs", cutoffDate);
        
        long deletedCount = auditLogRepository.deleteByCreatedAtBefore(cutoffDate);
        
        auditLog("AUDIT_LOGS_CLEANUP", "AUDIT", null,
                String.format("Cleaned up %d audit logs older than %s", deletedCount, cutoffDate));
        
        log.info("Cleaned up {} old audit logs", deletedCount);
        logMethodExit("cleanupOldAuditLogs", deletedCount);
        return deletedCount;
    }

    /**
     * Export audit logs for external compliance tools
     */
    public List<AuditLog> exportAuditLogs(OffsetDateTime startDate, OffsetDateTime endDate) {
        logMethodEntry("exportAuditLogs", startDate, endDate);
        
        List<AuditLog> auditLogs = auditLogRepository
            .findByCreatedAtBetweenOrderByCreatedAtAsc(startDate, endDate);
        
        auditLog("AUDIT_LOGS_EXPORTED", "AUDIT", null,
                String.format("Exported %d audit logs for period %s to %s", 
                             auditLogs.size(), startDate, endDate));
        
        logMethodExit("exportAuditLogs", auditLogs.size());
        return auditLogs;
    }

    /**
     * Track entity changes with before/after values
     */
    @Transactional
    public AuditLog trackEntityChange(String action, String entityType, Long entityId,
                                     Object oldEntity, Object newEntity, String correlationId) {
        logMethodEntry("trackEntityChange", action, entityType, entityId);
        
        Map<String, Object> oldValues = convertEntityToMap(oldEntity);
        Map<String, Object> newValues = convertEntityToMap(newEntity);
        
        String currentUser = getCurrentUsername();
        
        AuditLog auditLog = createAuditLog(action, entityType, entityId, oldValues, newValues,
                                          correlationId, currentUser, null, null);
        
        logMethodExit("trackEntityChange", auditLog.getId());
        return auditLog;
    }

    // Private helper methods

    private java.net.InetAddress parseIpAddress(String ipAddress) {
        if (ipAddress == null) {
            return null;
        }
        
        try {
            return java.net.InetAddress.getByName(ipAddress);
        } catch (Exception e) {
            log.warn("Failed to parse IP address: {}", ipAddress);
            return null;
        }
    }

    private Map<String, Object> convertEntityToMap(Object entity) {
        if (entity == null) {
            return null;
        }
        
        // Simple implementation - in production, you might use reflection or Jackson
        Map<String, Object> map = new HashMap<>();
        map.put("entity", entity.toString());
        
        // TODO: Implement proper entity to map conversion using reflection or ObjectMapper
        
        return map;
    }
}