package com.enterprise.payment.dto.analytics;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardStatsResponse {

    @JsonProperty("overview")
    private OverviewStats overview;

    @JsonProperty("payments")
    private PaymentStats payments;

    @JsonProperty("transactions")
    private TransactionStats transactions;

    @JsonProperty("accounts")
    private AccountStats accounts;

    @JsonProperty("trends")
    private TrendStats trends;

    @JsonProperty("topMetrics")
    private TopMetrics topMetrics;

    @JsonProperty("alerts")
    private List<AlertInfo> alerts;

    @JsonProperty("generatedAt")
    private OffsetDateTime generatedAt = OffsetDateTime.now();

    @JsonProperty("dataRange")
    private DateRange dataRange;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OverviewStats {
        @JsonProperty("totalRevenue")
        private BigDecimal totalRevenue;

        @JsonProperty("totalPayments")
        private Long totalPayments;

        @JsonProperty("successRate")
        private Double successRate;

        @JsonProperty("activeAccounts")
        private Long activeAccounts;

        @JsonProperty("averageTransactionValue")
        private BigDecimal averageTransactionValue;

        @JsonProperty("totalUsers")
        private Long totalUsers;

        @JsonProperty("processingFees")
        private BigDecimal processingFees;

        @JsonProperty("netRevenue")
        private BigDecimal netRevenue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PaymentStats {
        @JsonProperty("totalAmount")
        private BigDecimal totalAmount;

        @JsonProperty("totalCount")
        private Long totalCount;

        @JsonProperty("successful")
        private CountAmount successful;

        @JsonProperty("failed")
        private CountAmount failed;

        @JsonProperty("pending")
        private CountAmount pending;

        @JsonProperty("refunded")
        private CountAmount refunded;

        @JsonProperty("cancelled")
        private CountAmount cancelled;

        @JsonProperty("byStatus")
        private Map<String, CountAmount> byStatus;

        @JsonProperty("byCurrency")
        private Map<String, CountAmount> byCurrency;

        @JsonProperty("byPaymentMethod")
        private Map<String, CountAmount> byPaymentMethod;

        @JsonProperty("averageAmount")
        private BigDecimal averageAmount;

        @JsonProperty("medianAmount")
        private BigDecimal medianAmount;

        @JsonProperty("largestPayment")
        private BigDecimal largestPayment;

        @JsonProperty("smallestPayment")
        private BigDecimal smallestPayment;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TransactionStats {
        @JsonProperty("totalTransactions")
        private Long totalTransactions;

        @JsonProperty("totalVolume")
        private BigDecimal totalVolume;

        @JsonProperty("successful")
        private CountAmount successful;

        @JsonProperty("failed")
        private CountAmount failed;

        @JsonProperty("averageProcessingTime")
        private Double averageProcessingTimeMs;

        @JsonProperty("medianProcessingTime")
        private Double medianProcessingTimeMs;

        @JsonProperty("totalProcessingFees")
        private BigDecimal totalProcessingFees;

        @JsonProperty("byType")
        private Map<String, CountAmount> byType;

        @JsonProperty("byProvider")
        private Map<String, CountAmount> byProvider;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AccountStats {
        @JsonProperty("totalAccounts")
        private Long totalAccounts;

        @JsonProperty("activeAccounts")
        private Long activeAccounts;

        @JsonProperty("inactiveAccounts")
        private Long inactiveAccounts;

        @JsonProperty("suspendedAccounts")
        private Long suspendedAccounts;

        @JsonProperty("newAccountsThisPeriod")
        private Long newAccountsThisPeriod;

        @JsonProperty("totalBalance")
        private BigDecimal totalBalance;

        @JsonProperty("averageBalance")
        private BigDecimal averageBalance;

        @JsonProperty("accountsWithPayments")
        private Long accountsWithPayments;

        @JsonProperty("accountsWithoutPayments")
        private Long accountsWithoutPayments;

        @JsonProperty("byStatus")
        private Map<String, Long> byStatus;

        @JsonProperty("byCurrency")
        private Map<String, CountAmount> byCurrency;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TrendStats {
        @JsonProperty("revenueGrowth")
        private GrowthMetric revenueGrowth;

        @JsonProperty("paymentGrowth")
        private GrowthMetric paymentGrowth;

        @JsonProperty("accountGrowth")
        private GrowthMetric accountGrowth;

        @JsonProperty("successRateChange")
        private GrowthMetric successRateChange;

        @JsonProperty("dailyTrends")
        private List<DailyTrend> dailyTrends;

        @JsonProperty("monthlyTrends")
        private List<MonthlyTrend> monthlyTrends;

        @JsonProperty("hourlyDistribution")
        private Map<Integer, CountAmount> hourlyDistribution;

        @JsonProperty("dayOfWeekDistribution")
        private Map<String, CountAmount> dayOfWeekDistribution;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TopMetrics {
        @JsonProperty("topAccountsByRevenue")
        private List<TopAccount> topAccountsByRevenue;

        @JsonProperty("topAccountsByVolume")
        private List<TopAccount> topAccountsByVolume;

        @JsonProperty("topPaymentMethods")
        private List<TopPaymentMethod> topPaymentMethods;

        @JsonProperty("topCurrencies")
        private List<TopCurrency> topCurrencies;

        @JsonProperty("topFailureReasons")
        private List<TopFailureReason> topFailureReasons;

        @JsonProperty("recentLargePayments")
        private List<RecentPayment> recentLargePayments;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CountAmount {
        @JsonProperty("count")
        private Long count;

        @JsonProperty("amount")
        private BigDecimal amount;

        @JsonProperty("percentage")
        private Double percentage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GrowthMetric {
        @JsonProperty("current")
        private BigDecimal current;

        @JsonProperty("previous")
        private BigDecimal previous;

        @JsonProperty("change")
        private BigDecimal change;

        @JsonProperty("changePercentage")
        private Double changePercentage;

        @JsonProperty("isPositive")
        private Boolean isPositive;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DailyTrend {
        @JsonProperty("date")
        private String date; // yyyy-MM-dd format

        @JsonProperty("revenue")
        private BigDecimal revenue;

        @JsonProperty("paymentCount")
        private Long paymentCount;

        @JsonProperty("successRate")
        private Double successRate;

        @JsonProperty("averageAmount")
        private BigDecimal averageAmount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MonthlyTrend {
        @JsonProperty("month")
        private String month; // yyyy-MM format

        @JsonProperty("revenue")
        private BigDecimal revenue;

        @JsonProperty("paymentCount")
        private Long paymentCount;

        @JsonProperty("successRate")
        private Double successRate;

        @JsonProperty("newAccounts")
        private Long newAccounts;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TopAccount {
        @JsonProperty("accountId")
        private Long accountId;

        @JsonProperty("accountName")
        private String accountName;

        @JsonProperty("accountNumber")
        private String accountNumber;

        @JsonProperty("revenue")
        private BigDecimal revenue;

        @JsonProperty("paymentCount")
        private Long paymentCount;

        @JsonProperty("successRate")
        private Double successRate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TopPaymentMethod {
        @JsonProperty("type")
        private String type;

        @JsonProperty("provider")
        private String provider;

        @JsonProperty("count")
        private Long count;

        @JsonProperty("amount")
        private BigDecimal amount;

        @JsonProperty("successRate")
        private Double successRate;

        @JsonProperty("percentage")
        private Double percentage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TopCurrency {
        @JsonProperty("currencyCode")
        private String currencyCode;

        @JsonProperty("count")
        private Long count;

        @JsonProperty("amount")
        private BigDecimal amount;

        @JsonProperty("percentage")
        private Double percentage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TopFailureReason {
        @JsonProperty("reason")
        private String reason;

        @JsonProperty("count")
        private Long count;

        @JsonProperty("percentage")
        private Double percentage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RecentPayment {
        @JsonProperty("paymentId")
        private Long paymentId;

        @JsonProperty("paymentReference")
        private String paymentReference;

        @JsonProperty("amount")
        private BigDecimal amount;

        @JsonProperty("currencyCode")
        private String currencyCode;

        @JsonProperty("accountName")
        private String accountName;

        @JsonProperty("createdAt")
        private OffsetDateTime createdAt;

        @JsonProperty("status")
        private String status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AlertInfo {
        @JsonProperty("type")
        private String type; // WARNING, ERROR, INFO

        @JsonProperty("title")
        private String title;

        @JsonProperty("message")
        private String message;

        @JsonProperty("severity")
        private String severity; // HIGH, MEDIUM, LOW

        @JsonProperty("timestamp")
        private OffsetDateTime timestamp;

        @JsonProperty("actionRequired")
        private Boolean actionRequired;

        @JsonProperty("category")
        private String category; // PAYMENT, ACCOUNT, SYSTEM, SECURITY
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DateRange {
        @JsonProperty("startDate")
        private OffsetDateTime startDate;

        @JsonProperty("endDate")
        private OffsetDateTime endDate;

        @JsonProperty("period")
        private String period; // TODAY, WEEK, MONTH, QUARTER, YEAR, CUSTOM
    }
}