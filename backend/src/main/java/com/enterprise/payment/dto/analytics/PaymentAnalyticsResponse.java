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
public class PaymentAnalyticsResponse {

    @JsonProperty("summary")
    private PaymentSummary summary;

    @JsonProperty("statusBreakdown")
    private Map<String, StatusMetrics> statusBreakdown;

    @JsonProperty("methodBreakdown")
    private Map<String, MethodMetrics> methodBreakdown;

    @JsonProperty("currencyBreakdown")
    private Map<String, CurrencyMetrics> currencyBreakdown;

    @JsonProperty("trends")
    private TrendAnalysis trends;

    @JsonProperty("riskAnalysis")
    private RiskAnalysis riskAnalysis;

    @JsonProperty("performanceMetrics")
    private PerformanceMetrics performanceMetrics;

    @JsonProperty("failureAnalysis")
    private FailureAnalysis failureAnalysis;

    @JsonProperty("timeDistribution")
    private TimeDistribution timeDistribution;

    @JsonProperty("geographicDistribution")
    private List<GeographicMetrics> geographicDistribution;

    @JsonProperty("comparisonPeriod")
    private ComparisonPeriod comparisonPeriod;

    @JsonProperty("generatedAt")
    private OffsetDateTime generatedAt = OffsetDateTime.now();

    @JsonProperty("filters")
    private AnalyticsFilters filters;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PaymentSummary {
        @JsonProperty("totalPayments")
        private Long totalPayments;

        @JsonProperty("totalAmount")
        private BigDecimal totalAmount;

        @JsonProperty("averageAmount")
        private BigDecimal averageAmount;

        @JsonProperty("medianAmount")
        private BigDecimal medianAmount;

        @JsonProperty("largestPayment")
        private BigDecimal largestPayment;

        @JsonProperty("smallestPayment")
        private BigDecimal smallestPayment;

        @JsonProperty("successRate")
        private Double successRate;

        @JsonProperty("failureRate")
        private Double failureRate;

        @JsonProperty("refundRate")
        private Double refundRate;

        @JsonProperty("cancellationRate")
        private Double cancellationRate;

        @JsonProperty("totalRefunded")
        private BigDecimal totalRefunded;

        @JsonProperty("netAmount")
        private BigDecimal netAmount;

        @JsonProperty("uniqueAccounts")
        private Long uniqueAccounts;

        @JsonProperty("repeatCustomers")
        private Long repeatCustomers;

        @JsonProperty("repeatCustomerRate")
        private Double repeatCustomerRate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class StatusMetrics {
        @JsonProperty("count")
        private Long count;

        @JsonProperty("amount")
        private BigDecimal amount;

        @JsonProperty("percentage")
        private Double percentage;

        @JsonProperty("amountPercentage")
        private Double amountPercentage;

        @JsonProperty("averageAmount")
        private BigDecimal averageAmount;

        @JsonProperty("growth")
        private GrowthInfo growth;
    }

    @Data
    @NoArgsConstructor  
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MethodMetrics {
        @JsonProperty("type")
        private String type;

        @JsonProperty("provider")
        private String provider;

        @JsonProperty("count")
        private Long count;

        @JsonProperty("amount")
        private BigDecimal amount;

        @JsonProperty("percentage")
        private Double percentage;

        @JsonProperty("successRate")
        private Double successRate;

        @JsonProperty("averageAmount")
        private BigDecimal averageAmount;

        @JsonProperty("averageProcessingTime")
        private Double averageProcessingTime;

        @JsonProperty("totalProcessingFees")
        private BigDecimal totalProcessingFees;

        @JsonProperty("growth")
        private GrowthInfo growth;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CurrencyMetrics {
        @JsonProperty("currencyCode")
        private String currencyCode;

        @JsonProperty("count")
        private Long count;

        @JsonProperty("amount")
        private BigDecimal amount;

        @JsonProperty("percentage")
        private Double percentage;

        @JsonProperty("successRate")
        private Double successRate;

        @JsonProperty("averageAmount")
        private BigDecimal averageAmount;

        @JsonProperty("exchangeRateImpact")
        private BigDecimal exchangeRateImpact;

        @JsonProperty("growth")
        private GrowthInfo growth;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TrendAnalysis {
        @JsonProperty("dailyTrends")
        private List<DailyPaymentTrend> dailyTrends;

        @JsonProperty("weeklyTrends")
        private List<WeeklyPaymentTrend> weeklyTrends;

        @JsonProperty("monthlyTrends")
        private List<MonthlyPaymentTrend> monthlyTrends;

        @JsonProperty("seasonality")
        private SeasonalityAnalysis seasonality;

        @JsonProperty("forecast")
        private ForecastData forecast;

        @JsonProperty("growthRate")
        private Double growthRate;

        @JsonProperty("volatility")
        private Double volatility;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RiskAnalysis {
        @JsonProperty("averageRiskScore")
        private BigDecimal averageRiskScore;

        @JsonProperty("highRiskPayments")
        private Long highRiskPayments;

        @JsonProperty("highRiskAmount")
        private BigDecimal highRiskAmount;

        @JsonProperty("riskDistribution")
        private Map<String, Long> riskDistribution; // LOW, MEDIUM, HIGH

        @JsonProperty("flaggedPayments")
        private Long flaggedPayments;

        @JsonProperty("fraudulentPayments")
        private Long fraudulentPayments;

        @JsonProperty("fraudRate")
        private Double fraudRate;

        @JsonProperty("riskTrends")
        private List<RiskTrend> riskTrends;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PerformanceMetrics {
        @JsonProperty("averageProcessingTime")
        private Double averageProcessingTime;

        @JsonProperty("medianProcessingTime")
        private Double medianProcessingTime;

        @JsonProperty("fastestProcessingTime")
        private Double fastestProcessingTime;

        @JsonProperty("slowestProcessingTime")
        private Double slowestProcessingTime;

        @JsonProperty("processingTimeDistribution")
        private Map<String, Long> processingTimeDistribution;

        @JsonProperty("throughput")
        private Double throughput; // payments per minute

        @JsonProperty("peakHours")
        private List<PeakHour> peakHours;

        @JsonProperty("systemLoad")
        private SystemLoadMetrics systemLoad;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FailureAnalysis {
        @JsonProperty("totalFailures")
        private Long totalFailures;

        @JsonProperty("failureRate")
        private Double failureRate;

        @JsonProperty("failureReasons")
        private Map<String, Long> failureReasons;

        @JsonProperty("failuresByMethod")
        private Map<String, Long> failuresByMethod;

        @JsonProperty("failuresByCurrency")
        private Map<String, Long> failuresByCurrency;

        @JsonProperty("failureTrends")
        private List<FailureTrend> failureTrends;

        @JsonProperty("mttr")
        private Double mttr; // Mean Time To Resolution

        @JsonProperty("retrySuccessRate")
        private Double retrySuccessRate;

        @JsonProperty("commonFailurePatterns")
        private List<FailurePattern> commonFailurePatterns;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TimeDistribution {
        @JsonProperty("hourlyDistribution")
        private Map<Integer, Long> hourlyDistribution;

        @JsonProperty("dayOfWeekDistribution")
        private Map<String, Long> dayOfWeekDistribution;

        @JsonProperty("monthlyDistribution")
        private Map<String, Long> monthlyDistribution;

        @JsonProperty("peakHours")
        private List<Integer> peakHours;

        @JsonProperty("lowActivityHours")
        private List<Integer> lowActivityHours;

        @JsonProperty("businessHoursVsNonBusiness")
        private BusinessHoursComparison businessHoursComparison;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GeographicMetrics {
        @JsonProperty("country")
        private String country;

        @JsonProperty("countryCode")
        private String countryCode;

        @JsonProperty("count")
        private Long count;

        @JsonProperty("amount")
        private BigDecimal amount;

        @JsonProperty("percentage")
        private Double percentage;

        @JsonProperty("successRate")
        private Double successRate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ComparisonPeriod {
        @JsonProperty("current")
        private PeriodMetrics current;

        @JsonProperty("previous")
        private PeriodMetrics previous;

        @JsonProperty("growth")
        private GrowthComparison growth;
    }

    // Additional nested classes for comprehensive analytics
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GrowthInfo {
        @JsonProperty("previousValue")
        private BigDecimal previousValue;

        @JsonProperty("currentValue")
        private BigDecimal currentValue;

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
    public static class DailyPaymentTrend {
        @JsonProperty("date")
        private String date;

        @JsonProperty("count")
        private Long count;

        @JsonProperty("amount")
        private BigDecimal amount;

        @JsonProperty("successRate")
        private Double successRate;

        @JsonProperty("averageAmount")
        private BigDecimal averageAmount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class WeeklyPaymentTrend {
        @JsonProperty("week")
        private String week;

        @JsonProperty("count")
        private Long count;

        @JsonProperty("amount")
        private BigDecimal amount;

        @JsonProperty("successRate")
        private Double successRate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MonthlyPaymentTrend {
        @JsonProperty("month")
        private String month;

        @JsonProperty("count")
        private Long count;

        @JsonProperty("amount")
        private BigDecimal amount;

        @JsonProperty("successRate")
        private Double successRate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SeasonalityAnalysis {
        @JsonProperty("hasSeasonality")
        private Boolean hasSeasonality;

        @JsonProperty("peakSeason")
        private String peakSeason;

        @JsonProperty("lowSeason")
        private String lowSeason;

        @JsonProperty("seasonalityStrength")
        private Double seasonalityStrength;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ForecastData {
        @JsonProperty("nextPeriodForecast")
        private BigDecimal nextPeriodForecast;

        @JsonProperty("confidence")
        private Double confidence;

        @JsonProperty("forecastPeriods")
        private List<ForecastPeriod> forecastPeriods;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ForecastPeriod {
        @JsonProperty("period")
        private String period;

        @JsonProperty("forecast")
        private BigDecimal forecast;

        @JsonProperty("lowerBound")
        private BigDecimal lowerBound;

        @JsonProperty("upperBound")
        private BigDecimal upperBound;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RiskTrend {
        @JsonProperty("date")
        private String date;

        @JsonProperty("averageRiskScore")
        private BigDecimal averageRiskScore;

        @JsonProperty("highRiskCount")
        private Long highRiskCount;

        @JsonProperty("fraudCount")
        private Long fraudCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PeakHour {
        @JsonProperty("hour")
        private Integer hour;

        @JsonProperty("count")
        private Long count;

        @JsonProperty("amount")
        private BigDecimal amount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SystemLoadMetrics {
        @JsonProperty("averageLoad")
        private Double averageLoad;

        @JsonProperty("peakLoad")
        private Double peakLoad;

        @JsonProperty("loadDistribution")
        private Map<String, Double> loadDistribution;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FailureTrend {
        @JsonProperty("date")
        private String date;

        @JsonProperty("failureCount")
        private Long failureCount;

        @JsonProperty("failureRate")
        private Double failureRate;

        @JsonProperty("topReason")
        private String topReason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FailurePattern {
        @JsonProperty("pattern")
        private String pattern;

        @JsonProperty("frequency")
        private Long frequency;

        @JsonProperty("impact")
        private String impact;

        @JsonProperty("recommendation")
        private String recommendation;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BusinessHoursComparison {
        @JsonProperty("businessHours")
        private HoursMetrics businessHours;

        @JsonProperty("nonBusinessHours")
        private HoursMetrics nonBusinessHours;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class HoursMetrics {
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
    public static class PeriodMetrics {
        @JsonProperty("totalPayments")
        private Long totalPayments;

        @JsonProperty("totalAmount")
        private BigDecimal totalAmount;

        @JsonProperty("successRate")
        private Double successRate;

        @JsonProperty("averageAmount")
        private BigDecimal averageAmount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GrowthComparison {
        @JsonProperty("paymentsGrowth")
        private Double paymentsGrowth;

        @JsonProperty("amountGrowth")
        private Double amountGrowth;

        @JsonProperty("successRateChange")
        private Double successRateChange;

        @JsonProperty("averageAmountGrowth")
        private Double averageAmountGrowth;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AnalyticsFilters {
        @JsonProperty("dateRange")
        private DateRange dateRange;

        @JsonProperty("accountIds")
        private List<Long> accountIds;

        @JsonProperty("paymentMethods")
        private List<String> paymentMethods;

        @JsonProperty("currencies")
        private List<String> currencies;

        @JsonProperty("statuses")
        private List<String> statuses;

        @JsonProperty("amountRange")
        private AmountRange amountRange;
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
        private String period;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AmountRange {
        @JsonProperty("min")
        private BigDecimal min;

        @JsonProperty("max")
        private BigDecimal max;
    }
}