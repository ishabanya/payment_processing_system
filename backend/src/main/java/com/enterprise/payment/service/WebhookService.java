package com.enterprise.payment.service;

import com.enterprise.payment.entity.Payment;
import com.enterprise.payment.entity.Webhook;
import com.enterprise.payment.entity.WebhookDelivery;
import com.enterprise.payment.repository.WebhookDeliveryRepository;
import com.enterprise.payment.repository.WebhookRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service for managing webhook delivery
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService extends BaseService {

    private final WebhookRepository webhookRepository;
    private final WebhookDeliveryRepository webhookDeliveryRepository;

    /**
     * Send payment webhook
     */
    @Async
    @CircuitBreaker(name = "webhook-delivery", fallbackMethod = "webhookDeliveryFallback")
    @Retry(name = "webhook-delivery")
    public CompletableFuture<Void> sendPaymentWebhook(Payment payment, String eventType) {
        return CompletableFuture.runAsync(() -> {
            try {
                logMethodEntry("sendPaymentWebhook", payment.getPaymentReference(), eventType);
                
                List<Webhook> webhooks = webhookRepository.findByAccountAndIsActiveTrue(payment.getAccount());
                
                for (Webhook webhook : webhooks) {
                    if (webhook.getEvents().contains(eventType)) {
                        deliverWebhook(webhook, payment, eventType);
                    }
                }
                
                auditLog("WEBHOOK_SENT", "PAYMENT", payment.getId(), 
                        "Webhook sent for event: " + eventType);
                
                logMethodExit("sendPaymentWebhook");
            } catch (Exception e) {
                log.error("Failed to send webhook for payment: {} event: {}", 
                         payment.getPaymentReference(), eventType, e);
            }
        });
    }

    private void deliverWebhook(Webhook webhook, Payment payment, String eventType) {
        try {
            // TODO: Implement actual webhook delivery logic
            log.info("Delivering webhook to: {} for payment: {} event: {}", 
                    webhook.getUrl(), payment.getPaymentReference(), eventType);
            
            // Create webhook delivery record
            WebhookDelivery delivery = new WebhookDelivery();
            delivery.setWebhook(webhook);
            delivery.setPayment(payment);
            delivery.setEventType(eventType);
            delivery.setUrl(webhook.getUrl());
            delivery.setStatus(WebhookDelivery.DeliveryStatus.PENDING);
            
            webhookDeliveryRepository.save(delivery);
            
        } catch (Exception e) {
            log.error("Failed to deliver webhook to: {} for payment: {}", 
                     webhook.getUrl(), payment.getPaymentReference(), e);
        }
    }

    public CompletableFuture<Void> webhookDeliveryFallback(Payment payment, String eventType, Exception ex) {
        log.error("Circuit breaker activated for webhook delivery: {} event: {}", 
                 payment.getPaymentReference(), eventType, ex);
        return CompletableFuture.completedFuture(null);
    }
}