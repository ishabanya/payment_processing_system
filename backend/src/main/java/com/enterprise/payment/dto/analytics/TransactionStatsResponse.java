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
public class TransactionStatsResponse {

    @JsonProperty("summary")
    private TransactionSummary summary;

    @JsonProperty("typeBreakdown")
    private Map<String, TypeMetrics> typeBreakdown;

    @JsonProperty("statusBreakdown")
    private Map<String, StatusMetrics> statusBreakdown;

    @JsonProperty("providerBreakdown")
    private Map<String, ProviderMetrics> providerBreakdown;

    @JsonProperty("performanceMetrics")
    private PerformanceMetrics performanceMetrics;

    @JsonProperty("feeAnalysis")
    private FeeAnalysis feeAnalysis;

    @JsonProperty("volumeAnalysis")
    private VolumeAnalysis volumeAnalysis;

    @JsonProperty("timeDistribution")
    private TimeDistribution timeDistribution;

    @JsonProperty("trends")
    private TrendAnalysis trends;

    @JsonProperty("comparison")
    private ComparisonAnalysis comparison;

    @JsonProperty("topTransactions")
    private TopTransactions topTransactions;

    @JsonProperty("anomalies")
    private List<AnomalyInfo> anomalies;

    @JsonProperty("generatedAt")
    private OffsetDateTime generatedAt = OffsetDateTime.now();

    @JsonProperty("filters")
    private TransactionFilters filters;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TransactionSummary {
        @JsonProperty("totalTransactions")
        private Long totalTransactions;

        @JsonProperty("totalVolume")
        private BigDecimal totalVolume;

        @JsonProperty("totalFees")
        private BigDecimal totalFees;

        @JsonProperty("netVolume")
        private BigDecimal netVolume;

        @JsonProperty("averageTransactionAmount")
        private BigDecimal averageTransactionAmount;

        @JsonProperty("medianTransactionAmount")
        private BigDecimal medianTransactionAmount;

        @JsonProperty("largestTransaction")
        private BigDecimal largestTransaction;

        @JsonProperty("smallestTransaction")
        private BigDecimal smallestTransaction;

        @JsonProperty("successRate")
        private Double successRate;

        @JsonProperty("averageProcessingTime")
        private Double averageProcessingTime;

        @JsonProperty("throughput")
        private Double throughput; // transactions per minute

        @JsonProperty("uniquePayments")
        private Long uniquePayments;

        @JsonProperty("refundTransactions")
        private Long refundTransactions;

        @JsonProperty("refundAmount")
        private BigDecimal refundAmount;

        @JsonProperty("chargebackTransactions")
        private Long chargebackTransactions;

        @JsonProperty("chargebackAmount")
        private BigDecimal chargebackAmount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TypeMetrics {
        @JsonProperty("type")
        private String type;

        @JsonProperty("count")
        private Long count;

        @JsonProperty("volume")
        private BigDecimal volume;

        @JsonProperty("percentage")
        private Double percentage;

        @JsonProperty("averageAmount")
        private BigDecimal averageAmount;

        @JsonProperty("successRate")
        private Double successRate;

        @JsonProperty("averageProcessingTime")
        private Double averageProcessingTime;

        @JsonProperty("totalFees")
        private BigDecimal totalFees;

        @JsonProperty("growth")
        private GrowthMetrics growth;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class StatusMetrics {
        @JsonProperty("status")
        private String status;

        @JsonProperty("count")
        private Long count;

        @JsonProperty("volume")
        private BigDecimal volume;

        @JsonProperty("percentage")
        private Double percentage;

        @JsonProperty("averageAmount")
        private BigDecimal averageAmount;

        @JsonProperty("averageProcessingTime")
        private Double averageProcessingTime;

        @JsonProperty("growth")
        private GrowthMetrics growth;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProviderMetrics {
        @JsonProperty("provider")
        private String provider;

        @JsonProperty("count")
        private Long count;

        @JsonProperty("volume")
        private BigDecimal volume;

        @JsonProperty("percentage")
        private Double percentage;

        @JsonProperty("successRate")
        private Double successRate;

        @JsonProperty("averageProcessingTime")
        private Double averageProcessingTime;

        @JsonProperty("totalFees")
        private BigDecimal totalFees;

        @JsonProperty("averageFeePercentage")
        private Double averageFeePercentage;

        @JsonProperty("uptime")
        private Double uptime;

        @JsonProperty("errorRate")
        private Double errorRate;

        @JsonProperty("growth")
        private GrowthMetrics growth;
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

        @JsonProperty("p95ProcessingTime")
        private Double p95ProcessingTime;

        @JsonProperty("p99ProcessingTime")
        private Double p99ProcessingTime;

        @JsonProperty("fastestTransaction")
        private Double fastestTransaction;

        @JsonProperty("slowestTransaction")
        private Double slowestTransaction;

        @JsonProperty("processingTimeDistribution")
        private Map<String, Long> processingTimeDistribution;

        @JsonProperty("slaCompliance")
        private Double slaCompliance;

        @JsonProperty("timeoutRate")
        private Double timeoutRate;

        @JsonProperty("retryRate")
        private Double retryRate;

        @JsonProperty("throughputTrends")
        private List<ThroughputTrend> throughputTrends;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FeeAnalysis {
        @JsonProperty("totalFees")
        private BigDecimal totalFees;

        @JsonProperty("averageFee")
        private BigDecimal averageFee;

        @JsonProperty("averageFeePercentage")
        private Double averageFeePercentage;

        @JsonProperty("feesByProvider")
        private Map<String, ProviderFeeMetrics> feesByProvider;

        @JsonProperty("feesByType")
        private Map<String, BigDecimal> feesByType;

        @JsonProperty("feeDistribution")
        private FeeDistribution feeDistribution;

        @JsonProperty("feeTrends")
        private List<FeeTrend> feeTrends;

        @JsonProperty("feeOptimizationOpportunities")
        private List<FeeOptimization> feeOptimizationOpportunities;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class VolumeAnalysis {
        @JsonProperty("totalVolume")
        private BigDecimal totalVolume;

        @JsonProperty("volumeGrowth")
        private Double volumeGrowth;

        @JsonProperty("volumeByHour")
        private Map<Integer, BigDecimal> volumeByHour;

        @JsonProperty("volumeByDay")
        private Map<String, BigDecimal> volumeByDay;

        @JsonProperty("volumeByMonth")
        private Map<String, BigDecimal> volumeByMonth;

        @JsonProperty("peakVolumePeriods")
        private List<PeakVolumePeriod> peakVolumePeriods;

        @JsonProperty("volumeDistribution")
        private VolumeDistribution volumeDistribution;

        @JsonProperty("concentrationRisk")
        private ConcentrationRisk concentrationRisk;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TimeDistribution {
        @JsonProperty("hourlyDistribution")
        private Map<Integer, TransactionCount> hourlyDistribution;

        @JsonProperty("dayOfWeekDistribution")
        private Map<String, TransactionCount> dayOfWeekDistribution;

        @JsonProperty("monthlyDistribution")
        private Map<String, TransactionCount> monthlyDistribution;

        @JsonProperty("businessVsNonBusinessHours")
        private BusinessHoursAnalysis businessVsNonBusinessHours;

        @JsonProperty("seasonalPatterns")
        private SeasonalPatterns seasonalPatterns;

        @JsonProperty("peakLoadAnalysis")
        private PeakLoadAnalysis peakLoadAnalysis;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TrendAnalysis {
        @JsonProperty("dailyTrends")
        private List<DailyTransactionTrend> dailyTrends;

        @JsonProperty("weeklyTrends")
        private List<WeeklyTransactionTrend> weeklyTrends;

        @JsonProperty("monthlyTrends")
        private List<MonthlyTransactionTrend> monthlyTrends;

        @JsonProperty("growthRate")
        private Double growthRate;

        @JsonProperty("volatility")
        private Double volatility;

        @JsonProperty("momentum")
        private Double momentum;

        @JsonProperty("forecast")
        private TransactionForecast forecast;

        @JsonProperty("trendDirection")
        private String trendDirection; // INCREASING, DECREASING, STABLE
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ComparisonAnalysis {
        @JsonProperty("currentPeriod")
        private PeriodMetrics currentPeriod;

        @JsonProperty("previousPeriod")
        private PeriodMetrics previousPeriod;

        @JsonProperty("yearOverYear")
        private PeriodComparison yearOverYear;

        @JsonProperty("monthOverMonth")
        private PeriodComparison monthOverMonth;

        @JsonProperty("weekOverWeek")
        private PeriodComparison weekOverWeek;

        @JsonProperty("performanceVsBenchmark")
        private BenchmarkComparison performanceVsBenchmark;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TopTransactions {
        @JsonProperty("largestTransactions")
        private List<TransactionDetail> largestTransactions;

        @JsonProperty("fastestTransactions")
        private List<TransactionDetail> fastestTransactions;

        @JsonProperty("slowestTransactions")
        private List<TransactionDetail> slowestTransactions;

        @JsonProperty("highestFeeTransactions")
        private List<TransactionDetail> highestFeeTransactions;

        @JsonProperty("mostRecentTransactions")
        private List<TransactionDetail> mostRecentTransactions;
    }

    // Nested helper classes
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GrowthMetrics {
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
    public static class ThroughputTrend {
        @JsonProperty("timestamp")
        private OffsetDateTime timestamp;

        @JsonProperty("throughput")
        private Double throughput;

        @JsonProperty("transactionCount")
        private Long transactionCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProviderFeeMetrics {
        @JsonProperty("totalFees")
        private BigDecimal totalFees;

        @JsonProperty("averageFee")
        private BigDecimal averageFee;

        @JsonProperty("feePercentage")
        private Double feePercentage;

        @JsonProperty("transactionCount")
        private Long transactionCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FeeDistribution {
        @JsonProperty("lowFee")
        private RangeMetrics lowFee; // 0-1%

        @JsonProperty("mediumFee")
        private RangeMetrics mediumFee; // 1-3%

        @JsonProperty("highFee")
        private RangeMetrics highFee; // 3%+
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RangeMetrics {
        @JsonProperty("count")
        private Long count;

        @JsonProperty("percentage")
        private Double percentage;

        @JsonProperty("totalAmount")
        private BigDecimal totalAmount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FeeTrend {
        @JsonProperty("date")
        private String date;

        @JsonProperty("totalFees")
        private BigDecimal totalFees;

        @JsonProperty("averageFeePercentage")
        private Double averageFeePercentage;

        @JsonProperty("transactionCount")
        private Long transactionCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FeeOptimization {
        @JsonProperty("provider")
        private String provider;

        @JsonProperty("currentFee")
        private BigDecimal currentFee;

        @JsonProperty("suggestedFee")
        private BigDecimal suggestedFee;

        @JsonProperty("potentialSavings")
        private BigDecimal potentialSavings;

        @JsonProperty("recommendation")
        private String recommendation;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PeakVolumePeriod {
        @JsonProperty("startTime")
        private OffsetDateTime startTime;

        @JsonProperty("endTime")
        private OffsetDateTime endTime;

        @JsonProperty("volume")
        private BigDecimal volume;

        @JsonProperty("transactionCount")
        private Long transactionCount;

        @JsonProperty("label")
        private String label; // e.g., "Morning Rush", "Evening Peak"
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class VolumeDistribution {
        @JsonProperty("smallTransactions")
        private RangeMetrics smallTransactions; // <$100

        @JsonProperty("mediumTransactions")
        private RangeMetrics mediumTransactions; // $100-$1000

        @JsonProperty("largeTransactions")
        private RangeMetrics largeTransactions; // >$1000
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ConcentrationRisk {
        @JsonProperty("top5AccountsPercentage")
        private Double top5AccountsPercentage;

        @JsonProperty("top10AccountsPercentage")
        private Double top10AccountsPercentage;

        @JsonProperty("giniCoefficient")
        private Double giniCoefficient;

        @JsonProperty("riskLevel")
        private String riskLevel; // LOW, MEDIUM, HIGH
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TransactionCount {
        @JsonProperty("count")
        private Long count;

        @JsonProperty("volume")
        private BigDecimal volume;

        @JsonProperty("percentage")
        private Double percentage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BusinessHoursAnalysis {
        @JsonProperty("businessHours")
        private HoursMetrics businessHours;

        @JsonProperty("nonBusinessHours")
        private HoursMetrics nonBusinessHours;

        @JsonProperty("businessHoursDefinition")
        private String businessHoursDefinition;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class HoursMetrics {
        @JsonProperty("count")
        private Long count;

        @JsonProperty("volume")
        private BigDecimal volume;

        @JsonProperty("percentage")
        private Double percentage;

        @JsonProperty("averageAmount")
        private BigDecimal averageAmount;

        @JsonProperty("successRate")
        private Double successRate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SeasonalPatterns {
        @JsonProperty("hasSeasonality")
        private Boolean hasSeasonality;

        @JsonProperty("peakMonth")
        private String peakMonth;

        @JsonProperty("lowMonth")
        private String lowMonth;

        @JsonProperty("seasonalityStrength")
        private Double seasonalityStrength;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PeakLoadAnalysis {
        @JsonProperty("peakHour")
        private Integer peakHour;

        @JsonProperty("peakHourLoad")
        private Long peakHourLoad;

        @JsonProperty("averageLoad")
        private Double averageLoad;

        @JsonProperty("loadVariation")
        private Double loadVariation;

        @JsonProperty("capacityUtilization")
        private Double capacityUtilization;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DailyTransactionTrend {
        @JsonProperty("date")
        private String date;

        @JsonProperty("count")
        private Long count;

        @JsonProperty("volume")
        private BigDecimal volume;

        @JsonProperty("successRate")
        private Double successRate;

        @JsonProperty("averageProcessingTime")
        private Double averageProcessingTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class WeeklyTransactionTrend {
        @JsonProperty("week")
        private String week;

        @JsonProperty("count")
        private Long count;

        @JsonProperty("volume")
        private BigDecimal volume;

        @JsonProperty("successRate")
        private Double successRate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MonthlyTransactionTrend {
        @JsonProperty("month")
        private String month;

        @JsonProperty("count")
        private Long count;

        @JsonProperty("volume")
        private BigDecimal volume;

        @JsonProperty("successRate")
        private Double successRate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TransactionForecast {
        @JsonProperty("nextPeriodForecast")
        private Long nextPeriodForecast;

        @JsonProperty("nextPeriodVolumeForecast")
        private BigDecimal nextPeriodVolumeForecast;

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

        @JsonProperty("forecastCount")
        private Long forecastCount;

        @JsonProperty("forecastVolume")
        private BigDecimal forecastVolume;

        @JsonProperty("lowerBound")
        private Long lowerBound;

        @JsonProperty("upperBound")
        private Long upperBound;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PeriodMetrics {
        @JsonProperty("count")
        private Long count;

        @JsonProperty("volume")
        private BigDecimal volume;

        @JsonProperty("successRate")
        private Double successRate;

        @JsonProperty("averageAmount")
        private BigDecimal averageAmount;

        @JsonProperty("averageProcessingTime")
        private Double averageProcessingTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PeriodComparison {
        @JsonProperty("countChange")
        private Double countChange;

        @JsonProperty("volumeChange")
        private Double volumeChange;

        @JsonProperty("successRateChange")
        private Double successRateChange;

        @JsonProperty("processingTimeChange")
        private Double processingTimeChange;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BenchmarkComparison {
        @JsonProperty("industryAverage")
        private BenchmarkMetrics industryAverage;

        @JsonProperty("currentPerformance")
        private BenchmarkMetrics currentPerformance;

        @JsonProperty("ranking")
        private String ranking; // TOP_QUARTILE, ABOVE_AVERAGE, BELOW_AVERAGE, BOTTOM_QUARTILE
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BenchmarkMetrics {
        @JsonProperty("successRate")
        private Double successRate;

        @JsonProperty("averageProcessingTime")
        private Double averageProcessingTime;

        @JsonProperty("throughput")
        private Double throughput;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TransactionDetail {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("reference")
        private String reference;

        @JsonProperty("amount")
        private BigDecimal amount;

        @JsonProperty("currency")
        private String currency;

        @JsonProperty("type")
        private String type;

        @JsonProperty("status")
        private String status;

        @JsonProperty("processingTime")
        private Double processingTime;

        @JsonProperty("fee")
        private BigDecimal fee;

        @JsonProperty("createdAt")
        private OffsetDateTime createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AnomalyInfo {
        @JsonProperty("type")
        private String type; // VOLUME_SPIKE, UNUSUAL_PATTERN, PERFORMANCE_DEGRADATION

        @JsonProperty("severity")
        private String severity; // LOW, MEDIUM, HIGH, CRITICAL

        @JsonProperty("description")
        private String description;

        @JsonProperty("detectedAt")
        private OffsetDateTime detectedAt;

        @JsonProperty("impact")
        private String impact;

        @JsonProperty("recommendation")
        private String recommendation;

        @JsonProperty("metrics")
        private Map<String, Object> metrics;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TransactionFilters {
        @JsonProperty("dateRange")
        private DateRange dateRange;

        @JsonProperty("types")
        private List<String> types;

        @JsonProperty("statuses")
        private List<String> statuses;

        @JsonProperty("providers")
        private List<String> providers;

        @JsonProperty("amountRange")
        private AmountRange amountRange;

        @JsonProperty("accountIds")
        private List<Long> accountIds;
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