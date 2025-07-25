/**
 * Enterprise Payment Processing System
 * 
 * @author Y Shabanya Kishore (yadaginishabanya@gmail.com)
 * @version 1.0.0
 * @since 2024-07-26
 * 
 * Main application class for the enterprise-grade payment processing platform.
 * Supports high-volume transaction processing with comprehensive security,
 * monitoring, and compliance features.
 */
package com.enterprise.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableCaching
@EnableTransactionManagement
public class PaymentSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentSystemApplication.class, args);
    }

}