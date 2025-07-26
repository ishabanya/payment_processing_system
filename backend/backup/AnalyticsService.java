package com.enterprise.payment.service;

import com.enterprise.payment.dto.analytics.PaymentAnalyticsResponse;
import com.enterprise.payment.dto.analytics.TransactionStatsResponse;
import com.enterprise.payment.entity.Account;
import com.enterprise.payment.entity.Payment;
import com.enterprise.payment.exception.AccountNotFoundException;
import com.enterprise.payment.repository.AccountRepository;
import com.enterprise.payment.repository.PaymentRepository;
import com.enterprise.payment.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for comprehensive analytics and reporting
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService extends BaseService {

    private final PaymentRepository paymentRepository;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    /**
     * Get payment analytics for a specific time period
     */
    @Cacheable(value = "payment-analytics", key = "#startDate + '-' + #endDate")
    public PaymentAnalyticsResponse getPaymentAnalytics(OffsetDateTime startDate, OffsetDateTime endDate) {
        logMethodEntry("getPaymentAnalytics", startDate, endDate);
        
        PaymentAnalyticsResponse analytics = new PaymentAnalyticsResponse();
        
        // Total payments
        Long totalPayments = paymentRepository.countByCreatedAtBetween(startDate, endDate);
        analytics.setTotalPayments(totalPayments);
        
        // Total amount processed
        BigDecimal totalAmount = paymentRepository.sumAmountByCreatedAtBetween(startDate, endDate);
        analytics.setTotalAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO);
        
        // Average payment amount
        if (totalPayments > 0 && totalAmount != null && totalAmount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal avgAmount = totalAmount.divide(BigDecimal.valueOf(totalPayments), 2, RoundingMode.HALF_UP);
            analytics.setAveragePaymentAmount(avgAmount);
        } else {
            analytics.setAveragePaymentAmount(BigDecimal.ZERO);
        }
        
        // Success rate
        Long successfulPayments = paymentRepository.countByStatusAndCreatedAtBetween(
            Payment.PaymentStatus.COMPLETED, startDate, endDate);
        if (totalPayments > 0) {
            BigDecimal successRate = BigDecimal.valueOf(successfulPayments)
                .divide(BigDecimal.valueOf(totalPayments), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
            analytics.setSuccessRate(successRate);
        } else {
            analytics.setSuccessRate(BigDecimal.ZERO);
        }
        
        // Payment status breakdown
        Map<String, Long> statusBreakdown = new HashMap<>();
        for (Payment.PaymentStatus status : Payment.PaymentStatus.values()) {
            Long count = paymentRepository.countByStatusAndCreatedAtBetween(status, startDate, endDate);
            statusBreakdown.put(status.name(), count);
        }
        analytics.setPaymentsByStatus(statusBreakdown);
        
        // Daily payment trends
        List<Object[]> dailyTrends = paymentRepository.findDailyPaymentTrends(startDate, endDate);
        analytics.setDailyTrends(dailyTrends);
        
        // Top payment methods
        List<Object[]> topPaymentMethods = paymentRepository.findTopPaymentMethods(startDate, endDate);
        analytics.setTopPaymentMethods(topPaymentMethods);
        
        // Set metadata
        analytics.setPeriodStart(startDate);
        analytics.setPeriodEnd(endDate);
        analytics.setGeneratedAt(OffsetDateTime.now());
        
        logMethodExit("getPaymentAnalytics", analytics);
        return analytics;
    }

    /**
     * Get transaction statistics
     */
    @Cacheable(value = "transaction-stats", key = "#startDate + '-' + #endDate")
    public TransactionStatsResponse getTransactionStatistics(OffsetDateTime startDate, OffsetDateTime endDate) {
        logMethodEntry("getTransactionStatistics", startDate, endDate);
        
        TransactionStatsResponse stats = new TransactionStatsResponse();
        
        // Total transactions
        Long totalTransactions = transactionRepository.countByCreatedAtBetween(startDate, endDate);
        stats.setTotalTransactions(totalTransactions);
        
        // Total transaction amount
        BigDecimal totalAmount = transactionRepository.sumAmountByCreatedAtBetween(startDate, endDate);
        stats.setTotalAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO);
        
        // Transaction volume trends
        List<Object[]> volumeTrends = transactionRepository.findTransactionVolumeTrends(startDate, endDate);
        stats.setVolumeTrends(volumeTrends);
        
        // Set metadata
        stats.setPeriodStart(startDate);
        stats.setPeriodEnd(endDate);
        stats.setGeneratedAt(OffsetDateTime.now());
        
        logMethodExit("getTransactionStatistics", stats);
        return stats;
    }

    /**
     * Get account analytics for a specific account
     */
    @Cacheable(value = "account-analytics", key = "#accountNumber + '-' + #days")
    public Map<String, Object> getAccountAnalytics(String accountNumber, int days) {
        logMethodEntry("getAccountAnalytics", accountNumber, days);
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new AccountNotFoundException(accountNumber));
            
        OffsetDateTime startDate = OffsetDateTime.now().minusDays(days);
        OffsetDateTime endDate = OffsetDateTime.now();
        
        Map<String, Object> analytics = new HashMap<>();
        
        // Basic account info
        analytics.put("accountNumber", account.getAccountNumber());
        analytics.put("accountName", account.getAccountName());
        analytics.put("currentBalance", account.getBalance());
        analytics.put("accountStatus", account.getStatus().toString());
        
        // Payment statistics
        Long totalPayments = paymentRepository.countByAccountAndCreatedAtBetween(account, startDate, endDate);
        analytics.put("totalPayments", totalPayments);
        
        BigDecimal totalPaymentAmount = paymentRepository.sumAmountByAccountAndCreatedAtBetween(account, startDate, endDate);
        analytics.put("totalPaymentAmount", totalPaymentAmount != null ? totalPaymentAmount : BigDecimal.ZERO);
        
        // Payment success rate
        Long successfulPayments = paymentRepository.countByAccountAndStatusAndCreatedAtBetween(
            account, Payment.PaymentStatus.COMPLETED, startDate, endDate);
        if (totalPayments > 0) {
            BigDecimal successRate = BigDecimal.valueOf(successfulPayments)
                .divide(BigDecimal.valueOf(totalPayments), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
            analytics.put("successRate", successRate);
        } else {
            analytics.put("successRate", BigDecimal.ZERO);
        }
        
        // Daily payment activity
        List<Object[]> dailyActivity = paymentRepository.findDailyPaymentActivityByAccount(account, startDate, endDate);
        analytics.put("dailyActivity", dailyActivity);
        
        // Payment method usage
        List<Object[]> paymentMethodUsage = paymentRepository.findPaymentMethodUsageByAccount(account, startDate, endDate);
        analytics.put("paymentMethodUsage", paymentMethodUsage);
        
        // Risk analysis
        Map<String, Object> riskAnalysis = getRiskAnalytics(account, startDate, endDate);
        analytics.put("riskAnalysis", riskAnalysis);
        
        // Set metadata
        analytics.put("periodStart", startDate);
        analytics.put("periodEnd", endDate);
        analytics.put("generatedAt", OffsetDateTime.now());
        
        logMethodExit("getAccountAnalytics", analytics);
        return analytics;
    }

    /**
     * Get revenue analytics
     */
    @Cacheable(value = "revenue-analytics", key = "#startDate + '-' + #endDate")
    public Map<String, Object> getRevenueAnalytics(OffsetDateTime startDate, OffsetDateTime endDate) {
        logMethodEntry("getRevenueAnalytics", startDate, endDate);
        
        Map<String, Object> analytics = new HashMap<>();
        
        // Total revenue (completed payments)
        BigDecimal totalRevenue = paymentRepository.sumAmountByStatusAndCreatedAtBetween(
            Payment.PaymentStatus.COMPLETED, startDate, endDate);
        analytics.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        
        // Pending revenue
        BigDecimal pendingRevenue = paymentRepository.sumAmountByStatusAndCreatedAtBetween(
            Payment.PaymentStatus.PENDING, startDate, endDate);
        analytics.put("pendingRevenue", pendingRevenue != null ? pendingRevenue : BigDecimal.ZERO);
        
        // Failed revenue (potential losses)
        BigDecimal failedRevenue = paymentRepository.sumAmountByStatusAndCreatedAtBetween(
            Payment.PaymentStatus.FAILED, startDate, endDate);
        analytics.put("failedRevenue", failedRevenue != null ? failedRevenue : BigDecimal.ZERO);
        
        // Revenue trends by day
        List<Object[]> revenueTrends = paymentRepository.findRevenueByDay(startDate, endDate);
        analytics.put("revenueTrends", revenueTrends);
        
        // Revenue by payment method
        List<Object[]> revenueByMethod = paymentRepository.findRevenueByPaymentMethod(startDate, endDate);
        analytics.put("revenueByPaymentMethod", revenueByMethod);
        
        // Top revenue generating accounts
        List<Object[]> topAccounts = paymentRepository.findTopAccountsByRevenue(startDate, endDate);
        analytics.put("topAccounts", topAccounts);
        
        // Calculate growth rate if we have previous period data
        OffsetDateTime previousStart = startDate.minus(ChronoUnit.DAYS.between(startDate, endDate), ChronoUnit.DAYS);
        BigDecimal previousRevenue = paymentRepository.sumAmountByStatusAndCreatedAtBetween(
            Payment.PaymentStatus.COMPLETED, previousStart, startDate);
        
        if (previousRevenue != null && previousRevenue.compareTo(BigDecimal.ZERO) > 0 && totalRevenue != null) {
            BigDecimal growthRate = totalRevenue.subtract(previousRevenue)
                .divide(previousRevenue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
            analytics.put("growthRate", growthRate);
        } else {
            analytics.put("growthRate", BigDecimal.ZERO);
        }
        
        // Set metadata
        analytics.put("periodStart", startDate);
        analytics.put("periodEnd", endDate);
        analytics.put("generatedAt", OffsetDateTime.now());
        
        logMethodExit("getRevenueAnalytics", analytics);
        return analytics;
    }

    /**
     * Get risk analytics
     */
    private Map<String, Object> getRiskAnalytics(Account account, OffsetDateTime startDate, OffsetDateTime endDate) {
        Map<String, Object> riskAnalysis = new HashMap<>();
        
        // Average risk score
        BigDecimal avgRiskScore = paymentRepository.findAverageRiskScoreByAccount(account, startDate, endDate);
        riskAnalysis.put("averageRiskScore", avgRiskScore != null ? avgRiskScore : BigDecimal.ZERO);
        
        // High risk payments count
        Long highRiskPayments = paymentRepository.countHighRiskPaymentsByAccount(account, startDate, endDate);
        riskAnalysis.put("highRiskPayments", highRiskPayments);
        
        // Risk score distribution
        List<Object[]> riskDistribution = paymentRepository.findRiskScoreDistributionByAccount(account, startDate, endDate);
        riskAnalysis.put("riskDistribution", riskDistribution);
        
        return riskAnalysis;
    }

    /**
     * Get performance metrics
     */
    @Cacheable(value = "performance-metrics", key = "#startDate + '-' + #endDate")
    public Map<String, Object> getPerformanceMetrics(OffsetDateTime startDate, OffsetDateTime endDate) {
        logMethodEntry("getPerformanceMetrics", startDate, endDate);
        
        Map<String, Object> metrics = new HashMap<>();
        
        // Processing time metrics
        Map<String, Object> processingTimes = getProcessingTimeMetrics(startDate, endDate);
        metrics.put("processingTimes", processingTimes);
        
        // System health metrics
        Map<String, Object> systemHealth = getSystemHealthMetrics(startDate, endDate);
        metrics.put("systemHealth", systemHealth);
        
        // Error rates
        Map<String, Object> errorRates = getErrorRateMetrics(startDate, endDate);
        metrics.put("errorRates", errorRates);
        
        // Set metadata
        metrics.put("periodStart", startDate);
        metrics.put("periodEnd", endDate);
        metrics.put("generatedAt", OffsetDateTime.now());
        
        logMethodExit("getPerformanceMetrics", metrics);
        return metrics;
    }

    /**
     * Generate executive summary
     */
    @Cacheable(value = "executive-summary", key = "#startDate + '-' + #endDate")
    public Map<String, Object> getExecutiveSummary(OffsetDateTime startDate, OffsetDateTime endDate) {
        logMethodEntry("getExecutiveSummary", startDate, endDate);
        
        Map<String, Object> summary = new HashMap<>();
        
        // Key metrics
        PaymentAnalyticsResponse paymentAnalytics = getPaymentAnalytics(startDate, endDate);
        summary.put("totalPayments", paymentAnalytics.getTotalPayments());
        summary.put("totalRevenue", paymentAnalytics.getTotalAmount());
        summary.put("successRate", paymentAnalytics.getSuccessRate());
        summary.put("averagePaymentAmount", paymentAnalytics.getAveragePaymentAmount());
        
        // Growth indicators
        Map<String, Object> revenueAnalytics = getRevenueAnalytics(startDate, endDate);
        summary.put("growthRate", revenueAnalytics.get("growthRate"));
        
        // Performance indicators
        Map<String, Object> performanceMetrics = getPerformanceMetrics(startDate, endDate);
        summary.put("systemPerformance", performanceMetrics);
        
        // Risk indicators
        Long totalHighRiskPayments = paymentRepository.countHighRiskPayments(startDate, endDate);
        summary.put("highRiskPayments", totalHighRiskPayments);
        
        // Active accounts
        Long activeAccounts = accountRepository.countActiveAccounts();
        summary.put("activeAccounts", activeAccounts);
        
        // Set metadata
        summary.put("periodStart", startDate);
        summary.put("periodEnd", endDate);
        summary.put("generatedAt", OffsetDateTime.now());
        summary.put("generatedBy", getCurrentUsername());
        
        // Create audit log for summary generation
        auditLog("EXECUTIVE_SUMMARY_GENERATED", "ANALYTICS", null,
                String.format("Executive summary generated for period %s to %s", startDate, endDate));
        
        logMethodExit("getExecutiveSummary", summary);
        return summary;
    }

    // Private helper methods

    private Map<String, Object> getProcessingTimeMetrics(OffsetDateTime startDate, OffsetDateTime endDate) {
        Map<String, Object> metrics = new HashMap<>();
        
        // Average processing time
        Double avgProcessingTime = paymentRepository.findAverageProcessingTime(startDate, endDate);
        metrics.put("averageProcessingTime", avgProcessingTime != null ? avgProcessingTime : 0.0);
        
        // Processing time percentiles
        List<Object[]> processingTimePercentiles = paymentRepository.findProcessingTimePercentiles(startDate, endDate);
        metrics.put("processingTimePercentiles", processingTimePercentiles);
        
        return metrics;
    }

    private Map<String, Object> getSystemHealthMetrics(OffsetDateTime startDate, OffsetDateTime endDate) {
        Map<String, Object> metrics = new HashMap<>();
        
        // System uptime (mock - would be actual system metrics in production)
        metrics.put("uptime", 99.9);
        
        // API response times (mock)
        metrics.put("averageResponseTime", 150.0);
        
        // Throughput
        Long totalRequests = paymentRepository.countByCreatedAtBetween(startDate, endDate);
        long periodHours = ChronoUnit.HOURS.between(startDate, endDate);
        if (periodHours > 0) {
            metrics.put("requestsPerHour", totalRequests / periodHours);
        } else {
            metrics.put("requestsPerHour", 0L);
        }
        
        return metrics;
    }

    private Map<String, Object> getErrorRateMetrics(OffsetDateTime startDate, OffsetDateTime endDate) {
        Map<String, Object> metrics = new HashMap<>();
        
        Long totalPayments = paymentRepository.countByCreatedAtBetween(startDate, endDate);
        Long failedPayments = paymentRepository.countByStatusAndCreatedAtBetween(
            Payment.PaymentStatus.FAILED, startDate, endDate);
        
        if (totalPayments > 0) {
            BigDecimal errorRate = BigDecimal.valueOf(failedPayments)
                .divide(BigDecimal.valueOf(totalPayments), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
            metrics.put("errorRate", errorRate);
        } else {
            metrics.put("errorRate", BigDecimal.ZERO);
        }
        
        metrics.put("totalErrors", failedPayments);
        
        return metrics;
    }
}