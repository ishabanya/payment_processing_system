package com.enterprise.payment.service;

import com.enterprise.payment.entity.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Service for fraud detection and risk scoring
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RiskAssessmentService extends BaseService {

    /**
     * Assess payment risk and return risk score (0-100)
     */
    @Cacheable(value = "risk-scores", key = "#payment.id")
    public BigDecimal assessPaymentRisk(Payment payment) {
        logMethodEntry("assessPaymentRisk", payment.getPaymentReference());
        
        try {
            BigDecimal riskScore = calculateRiskScore(payment);
            
            log.info("Risk assessment completed for payment: {} score: {}", 
                    payment.getPaymentReference(), riskScore);
            
            auditLog("RISK_ASSESSMENT", "PAYMENT", payment.getId(), 
                    "Risk assessment completed with score: " + riskScore);
            
            logMethodExit("assessPaymentRisk", riskScore);
            return riskScore;
            
        } catch (Exception e) {
            log.error("Risk assessment failed for payment: {}", payment.getPaymentReference(), e);
            // Return medium risk score as fallback
            return BigDecimal.valueOf(50.0);
        }
    }

    private BigDecimal calculateRiskScore(Payment payment) {
        BigDecimal score = BigDecimal.ZERO;
        
        // Amount-based risk (higher amounts = higher risk)
        score = score.add(calculateAmountRisk(payment.getAmount()));
        
        // Account history risk
        score = score.add(calculateAccountHistoryRisk(payment));
        
        // Time-based risk (late night transactions)
        score = score.add(calculateTimeBasedRisk());
        
        // Payment method risk
        score = score.add(calculatePaymentMethodRisk(payment));
        
        // Geography-based risk (mock implementation)
        score = score.add(calculateGeographyRisk());
        
        // Velocity risk (multiple transactions in short time)
        score = score.add(calculateVelocityRisk(payment));
        
        // Ensure score is between 0 and 100
        if (score.compareTo(BigDecimal.ZERO) < 0) {
            score = BigDecimal.ZERO;
        }
        if (score.compareTo(BigDecimal.valueOf(100)) > 0) {
            score = BigDecimal.valueOf(100);
        }
        
        return score.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateAmountRisk(BigDecimal amount) {
        // Higher amounts have higher risk scores
        if (amount.compareTo(BigDecimal.valueOf(10000)) > 0) {
            return BigDecimal.valueOf(30.0); // High amount risk
        } else if (amount.compareTo(BigDecimal.valueOf(1000)) > 0) {
            return BigDecimal.valueOf(15.0); // Medium amount risk
        } else {
            return BigDecimal.valueOf(5.0);  // Low amount risk
        }
    }

    private BigDecimal calculateAccountHistoryRisk(Payment payment) {
        // TODO: Implement actual account history analysis
        // Mock implementation - check account age, previous transactions
        
        if (payment.getAccount().getCreatedAt().isAfter(
                payment.getCreatedAt().minusDays(30))) {
            return BigDecimal.valueOf(25.0); // New account risk
        } else if (payment.getAccount().getCreatedAt().isAfter(
                payment.getCreatedAt().minusDays(90))) {
            return BigDecimal.valueOf(10.0); // Recent account risk
        } else {
            return BigDecimal.valueOf(2.0);  // Established account
        }
    }

    private BigDecimal calculateTimeBasedRisk() {
        // TODO: Implement time-based risk analysis
        // Mock implementation - transactions at unusual hours have higher risk
        
        int hour = java.time.LocalTime.now().getHour();
        if (hour >= 23 || hour <= 5) {
            return BigDecimal.valueOf(10.0); // Late night/early morning risk
        } else {
            return BigDecimal.valueOf(2.0);  // Normal hours
        }
    }

    private BigDecimal calculatePaymentMethodRisk(Payment payment) {
        // TODO: Implement payment method risk analysis
        // Mock implementation
        
        if (payment.getPaymentMethod() == null) {
            return BigDecimal.valueOf(15.0); // No payment method specified
        } else {
            return BigDecimal.valueOf(5.0);  // Payment method available
        }
    }

    private BigDecimal calculateGeographyRisk() {
        // TODO: Implement geography-based risk analysis
        // Mock implementation - could check IP location, billing address, etc.
        
        return BigDecimal.valueOf(Math.random() * 10); // Random geography risk 0-10
    }

    private BigDecimal calculateVelocityRisk(Payment payment) {
        // TODO: Implement velocity risk analysis
        // Mock implementation - check for multiple transactions in short time
        
        // This would typically query recent transactions from the same account
        return BigDecimal.valueOf(Math.random() * 15); // Random velocity risk 0-15
    }

    /**
     * Check if payment should be automatically blocked based on risk score
     */
    public boolean shouldBlockPayment(BigDecimal riskScore) {
        return riskScore.compareTo(BigDecimal.valueOf(80.0)) > 0;
    }

    /**
     * Check if payment requires manual review based on risk score
     */
    public boolean requiresManualReview(BigDecimal riskScore) {
        return riskScore.compareTo(BigDecimal.valueOf(60.0)) > 0 && 
               riskScore.compareTo(BigDecimal.valueOf(80.0)) <= 0;
    }
}