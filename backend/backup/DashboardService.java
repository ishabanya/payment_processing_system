package com.enterprise.payment.service;

import com.enterprise.payment.dto.analytics.DashboardStatsResponse;
import com.enterprise.payment.entity.Account;
import com.enterprise.payment.entity.Payment;
import com.enterprise.payment.repository.AccountRepository;
import com.enterprise.payment.repository.PaymentRepository;
import com.enterprise.payment.repository.TransactionRepository;
import com.enterprise.payment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for dashboard data aggregation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService extends BaseService {

    private final PaymentRepository paymentRepository;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AnalyticsService analyticsService;

    /**
     * Get comprehensive dashboard statistics
     */
    @Cacheable(value = "dashboard-stats", key = "'all'")
    public DashboardStatsResponse getDashboardStats() {
        logMethodEntry("getDashboardStats");
        
        DashboardStatsResponse stats = new DashboardStatsResponse();
        
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime today = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime yesterday = today.minusDays(1);
        OffsetDateTime thisMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime lastMonth = thisMonth.minusMonths(1);
        
        // Basic counts
        stats.setTotalAccounts(accountRepository.count());
        stats.setActiveAccounts(accountRepository.countByStatus(Account.AccountStatus.ACTIVE));
        stats.setTotalUsers(userRepository.count());
        stats.setActiveUsers(userRepository.countByIsActive(true));
        
        // Payment statistics
        stats.setTotalPayments(paymentRepository.count());
        stats.setTodayPayments(paymentRepository.countByCreatedAtBetween(today, now));
        stats.setThisMonthPayments(paymentRepository.countByCreatedAtBetween(thisMonth, now));
        
        // Revenue statistics
        BigDecimal totalRevenue = paymentRepository.sumAmountByStatus(Payment.PaymentStatus.COMPLETED);
        stats.setTotalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        
        BigDecimal todayRevenue = paymentRepository.sumAmountByStatusAndCreatedAtBetween(
            Payment.PaymentStatus.COMPLETED, today, now);
        stats.setTodayRevenue(todayRevenue != null ? todayRevenue : BigDecimal.ZERO);
        
        BigDecimal thisMonthRevenue = paymentRepository.sumAmountByStatusAndCreatedAtBetween(
            Payment.PaymentStatus.COMPLETED, thisMonth, now);
        stats.setThisMonthRevenue(thisMonthRevenue != null ? thisMonthRevenue : BigDecimal.ZERO);
        
        // Calculate growth rates
        calculateGrowthRates(stats, yesterday, today, lastMonth, thisMonth, now);
        
        // Success rates
        calculateSuccessRates(stats, today, now);
        
        // Recent activity
        stats.setRecentPayments(getRecentPayments(10));
        stats.setTopAccounts(getTopAccounts(5));
        
        // Payment status distribution
        stats.setPaymentStatusDistribution(getPaymentStatusDistribution());
        
        // Performance metrics
        stats.setPerformanceMetrics(getPerformanceMetrics());
        
        // Set metadata
        stats.setGeneratedAt(now);
        stats.setLastUpdated(now);
        
        logMethodExit("getDashboardStats", stats);
        return stats;
    }

    /**
     * Get dashboard statistics for a specific account
     */
    @Cacheable(value = "account-dashboard", key = "#accountId")
    public Map<String, Object> getAccountDashboard(Long accountId) {
        logMethodEntry("getAccountDashboard", accountId);
        
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new RuntimeException("Account not found: " + accountId));
            
        Map<String, Object> dashboard = new HashMap<>();
        
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime today = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime thisMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        
        // Account info
        dashboard.put("accountNumber", account.getAccountNumber());
        dashboard.put("accountName", account.getAccountName());
        dashboard.put("currentBalance", account.getBalance());
        dashboard.put("accountStatus", account.getStatus().toString());
        
        // Payment statistics
        Long totalPayments = paymentRepository.countByAccount(account);
        dashboard.put("totalPayments", totalPayments);
        
        Long todayPayments = paymentRepository.countByAccountAndCreatedAtBetween(account, today, now);
        dashboard.put("todayPayments", todayPayments);
        
        Long thisMonthPayments = paymentRepository.countByAccountAndCreatedAtBetween(account, thisMonth, now);
        dashboard.put("thisMonthPayments", thisMonthPayments);
        
        // Revenue for this account
        BigDecimal totalRevenue = paymentRepository.sumAmountByAccountAndStatus(account, Payment.PaymentStatus.COMPLETED);
        dashboard.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        
        // Success rate
        Long successfulPayments = paymentRepository.countByAccountAndStatus(account, Payment.PaymentStatus.COMPLETED);
        if (totalPayments > 0) {
            BigDecimal successRate = BigDecimal.valueOf(successfulPayments)
                .divide(BigDecimal.valueOf(totalPayments), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
            dashboard.put("successRate", successRate);
        } else {
            dashboard.put("successRate", BigDecimal.ZERO);
        }
        
        // Recent payments for this account
        List<Map<String, Object>> recentPayments = getRecentPaymentsForAccount(account, 5);
        dashboard.put("recentPayments", recentPayments);
        
        // Payment trends
        List<Object[]> paymentTrends = paymentRepository.findDailyPaymentActivityByAccount(
            account, now.minusDays(30), now);
        dashboard.put("paymentTrends", paymentTrends);
        
        // Risk analysis
        Map<String, Object> riskAnalysis = analyticsService.getAccountAnalytics(
            account.getAccountNumber(), 30);
        dashboard.put("riskAnalysis", riskAnalysis.get("riskAnalysis"));
        
        // Set metadata
        dashboard.put("generatedAt", now);
        
        logMethodExit("getAccountDashboard", dashboard);
        return dashboard;
    }

    /**
     * Get real-time metrics
     */
    @Cacheable(value = "realtime-metrics", key = "'current'", unless = "#result == null")
    public Map<String, Object> getRealTimeMetrics() {
        logMethodEntry("getRealTimeMetrics");
        
        Map<String, Object> metrics = new HashMap<>();
        
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime lastHour = now.minusHours(1);
        OffsetDateTime last24Hours = now.minusHours(24);
        
        // Real-time payment activity
        Long paymentsLastHour = paymentRepository.countByCreatedAtBetween(lastHour, now);
        metrics.put("paymentsLastHour", paymentsLastHour);
        
        Long paymentsLast24Hours = paymentRepository.countByCreatedAtBetween(last24Hours, now);
        metrics.put("paymentsLast24Hours", paymentsLast24Hours);
        
        // Processing queue status
        Long pendingPayments = paymentRepository.countByStatus(Payment.PaymentStatus.PENDING);
        metrics.put("pendingPayments", pendingPayments);
        
        Long processingPayments = paymentRepository.countByStatus(Payment.PaymentStatus.PROCESSING);
        metrics.put("processingPayments", processingPayments);
        
        // Active sessions (mock data - would come from session store in production)
        metrics.put("activeSessions", 150);
        
        // System health indicators
        metrics.put("systemHealth", "HEALTHY");
        metrics.put("apiResponseTime", 120.5);
        metrics.put("databaseConnections", 8);
        
        // Alert counts
        metrics.put("activeAlerts", 2);
        metrics.put("criticalAlerts", 0);
        
        // Set timestamp
        metrics.put("timestamp", now);
        
        logMethodExit("getRealTimeMetrics", metrics);
        return metrics;
    }

    /**
     * Get trending data for charts
     */
    @Cacheable(value = "trending-data", key = "#days")
    public Map<String, Object> getTrendingData(int days) {
        logMethodEntry("getTrendingData", days);
        
        Map<String, Object> trends = new HashMap<>();
        
        OffsetDateTime endDate = OffsetDateTime.now();
        OffsetDateTime startDate = endDate.minusDays(days);
        
        // Payment volume trends
        List<Object[]> paymentVolume = paymentRepository.findDailyPaymentTrends(startDate, endDate);
        trends.put("paymentVolume", paymentVolume);
        
        // Revenue trends
        List<Object[]> revenue = paymentRepository.findRevenueByDay(startDate, endDate);
        trends.put("revenue", revenue);
        
        // Success rate trends
        List<Object[]> successRates = paymentRepository.findDailySuccessRates(startDate, endDate);
        trends.put("successRates", successRates);
        
        // User registration trends
        List<Object[]> userRegistrations = userRepository.findDailyRegistrations(startDate, endDate);
        trends.put("userRegistrations", userRegistrations);
        
        // Account creation trends
        List<Object[]> accountCreations = accountRepository.findDailyAccountCreations(startDate, endDate);
        trends.put("accountCreations", accountCreations);
        
        // Set metadata
        trends.put("periodStart", startDate);
        trends.put("periodEnd", endDate);
        trends.put("generatedAt", OffsetDateTime.now());
        
        logMethodExit("getTrendingData", trends);
        return trends;
    }

    /**
     * Get alert dashboard
     */
    public Map<String, Object> getAlertDashboard() {
        logMethodEntry("getAlertDashboard");
        
        Map<String, Object> alerts = new HashMap<>();
        
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime lastHour = now.minusHours(1);
        
        // High-risk payment alerts
        Long highRiskPayments = paymentRepository.countHighRiskPayments(lastHour, now);
        alerts.put("highRiskPayments", highRiskPayments);
        
        // Failed payment alerts
        Long failedPayments = paymentRepository.countByStatusAndCreatedAtBetween(
            Payment.PaymentStatus.FAILED, lastHour, now);
        alerts.put("failedPayments", failedPayments);
        
        // System performance alerts
        // In production, these would come from monitoring systems
        alerts.put("systemAlerts", List.of());
        
        // Security alerts
        alerts.put("securityAlerts", List.of());
        
        // Data quality alerts
        alerts.put("dataQualityAlerts", List.of());
        
        // Set metadata
        alerts.put("generatedAt", now);
        
        logMethodExit("getAlertDashboard", alerts);
        return alerts;
    }

    // Private helper methods

    private void calculateGrowthRates(DashboardStatsResponse stats, OffsetDateTime yesterday, 
                                     OffsetDateTime today, OffsetDateTime lastMonth, 
                                     OffsetDateTime thisMonth, OffsetDateTime now) {
        // Yesterday's payments
        Long yesterdayPayments = paymentRepository.countByCreatedAtBetween(yesterday, today);
        if (yesterdayPayments > 0 && stats.getTodayPayments() > 0) {
            BigDecimal dailyGrowth = BigDecimal.valueOf(stats.getTodayPayments() - yesterdayPayments)
                .divide(BigDecimal.valueOf(yesterdayPayments), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
            stats.setDailyGrowthRate(dailyGrowth);
        } else {
            stats.setDailyGrowthRate(BigDecimal.ZERO);
        }
        
        // Last month's payments
        Long lastMonthPayments = paymentRepository.countByCreatedAtBetween(lastMonth, thisMonth);
        if (lastMonthPayments > 0 && stats.getThisMonthPayments() > 0) {
            BigDecimal monthlyGrowth = BigDecimal.valueOf(stats.getThisMonthPayments() - lastMonthPayments)
                .divide(BigDecimal.valueOf(lastMonthPayments), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
            stats.setMonthlyGrowthRate(monthlyGrowth);
        } else {
            stats.setMonthlyGrowthRate(BigDecimal.ZERO);
        }
    }

    private void calculateSuccessRates(DashboardStatsResponse stats, OffsetDateTime today, OffsetDateTime now) {
        Long todaySuccessful = paymentRepository.countByStatusAndCreatedAtBetween(
            Payment.PaymentStatus.COMPLETED, today, now);
        
        if (stats.getTodayPayments() > 0) {
            BigDecimal successRate = BigDecimal.valueOf(todaySuccessful)
                .divide(BigDecimal.valueOf(stats.getTodayPayments()), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
            stats.setSuccessRate(successRate);
        } else {
            stats.setSuccessRate(BigDecimal.ZERO);
        }
    }

    private List<Map<String, Object>> getRecentPayments(int limit) {
        // This would be implemented with a proper repository query
        return List.of(); // Placeholder
    }

    private List<Map<String, Object>> getTopAccounts(int limit) {
        OffsetDateTime thirtyDaysAgo = OffsetDateTime.now().minusDays(30);
        List<Object[]> topAccounts = paymentRepository.findTopAccountsByRevenue(thirtyDaysAgo, OffsetDateTime.now());
        
        return topAccounts.stream()
            .limit(limit)
            .map(row -> {
                Map<String, Object> account = new HashMap<>();
                account.put("accountNumber", row[0]);
                account.put("revenue", row[1]);
                account.put("paymentCount", row[2]);
                return account;
            })
            .toList();
    }

    private Map<String, Long> getPaymentStatusDistribution() {
        Map<String, Long> distribution = new HashMap<>();
        for (Payment.PaymentStatus status : Payment.PaymentStatus.values()) {
            Long count = paymentRepository.countByStatus(status);
            distribution.put(status.name(), count);
        }
        return distribution;
    }

    private Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Average processing time for completed payments
        OffsetDateTime yesterday = OffsetDateTime.now().minusDays(1);
        Double avgProcessingTime = paymentRepository.findAverageProcessingTime(yesterday, OffsetDateTime.now());
        metrics.put("averageProcessingTime", avgProcessingTime != null ? avgProcessingTime : 0.0);
        
        // System throughput (payments per hour)
        Long lastHourPayments = paymentRepository.countByCreatedAtBetween(
            OffsetDateTime.now().minusHours(1), OffsetDateTime.now());
        metrics.put("paymentsPerHour", lastHourPayments);
        
        // Mock system metrics (in production, these would come from monitoring)
        metrics.put("cpuUsage", 45.2);
        metrics.put("memoryUsage", 67.8);
        metrics.put("diskUsage", 23.1);
        
        return metrics;
    }

    private List<Map<String, Object>> getRecentPaymentsForAccount(Account account, int limit) {
        // This would be implemented with a proper repository query
        return List.of(); // Placeholder
    }
}