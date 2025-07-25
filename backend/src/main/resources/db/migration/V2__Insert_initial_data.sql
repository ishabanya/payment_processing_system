-- Insert sample accounts
INSERT INTO accounts (account_number, account_name, email, phone, balance, currency_code, created_by) VALUES
('ACC000001', 'Enterprise Corp', 'admin@enterprise.com', '+1-555-0001', 50000.00, 'USD', 'system'),
('ACC000002', 'TechStart Inc', 'contact@techstart.com', '+1-555-0002', 25000.00, 'USD', 'system'),
('ACC000003', 'Global Payments Ltd', 'info@globalpay.com', '+1-555-0003', 100000.00, 'USD', 'system');

-- Insert sample users (password is 'password123' hashed with BCrypt)
INSERT INTO users (username, email, password_hash, first_name, last_name, role, account_id, created_at) VALUES
('admin', 'admin@enterprise.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/lewdBcQu8ZCW5nMV2', 'Admin', 'User', 'ADMIN', 1, CURRENT_TIMESTAMP),
('merchant1', 'merchant@techstart.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/lewdBcQu8ZCW5nMV2', 'John', 'Merchant', 'MERCHANT', 2, CURRENT_TIMESTAMP),
('user1', 'user@globalpay.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/lewdBcQu8ZCW5nMV2', 'Jane', 'User', 'USER', 3, CURRENT_TIMESTAMP);

-- Insert sample payment methods
INSERT INTO payment_methods (account_id, type, provider, encrypted_details, last_four_digits, expiry_month, expiry_year, is_default) VALUES
(1, 'CREDIT_CARD', 'Visa', 'encrypted_card_data_1', '1234', 12, 2025, true),
(1, 'BANK_TRANSFER', 'Chase Bank', 'encrypted_bank_data_1', '5678', null, null, false),
(2, 'CREDIT_CARD', 'MasterCard', 'encrypted_card_data_2', '9012', 10, 2024, true),
(3, 'DIGITAL_WALLET', 'PayPal', 'encrypted_wallet_data_1', null, null, null, true);

-- Insert sample payments
INSERT INTO payments (payment_reference, account_id, payment_method_id, amount, currency_code, description, status, merchant_reference, metadata) VALUES
('PAY-2024-001', 1, 1, 1000.00, 'USD', 'Product Purchase - Enterprise License', 'COMPLETED', 'ORD-001', '{"order_id": "ORD-001", "product": "Enterprise License"}'),
('PAY-2024-002', 2, 3, 250.50, 'USD', 'Monthly Subscription', 'PROCESSING', 'SUB-002', '{"subscription_id": "SUB-002", "plan": "Pro"}'),
('PAY-2024-003', 3, 4, 75.99, 'USD', 'Digital Product Purchase', 'PENDING', 'DIG-003', '{"product_type": "digital", "category": "software"}'),
('PAY-2024-004', 1, 2, 5000.00, 'USD', 'Bulk Payment Processing', 'FAILED', 'BULK-004', '{"batch_id": "BATCH-001", "items": 50}'),
('PAY-2024-005', 2, 3, 199.99, 'USD', 'Annual Premium Upgrade', 'COMPLETED', 'UPG-005', '{"upgrade_from": "basic", "upgrade_to": "premium"}');

-- Insert sample transactions
INSERT INTO transactions (transaction_reference, payment_id, type, amount, currency_code, status, gateway_transaction_id, processing_fee, processed_at) VALUES
('TXN-2024-001', 1, 'PAYMENT', 1000.00, 'USD', 'COMPLETED', 'GTW-12345', 29.00, CURRENT_TIMESTAMP - INTERVAL '1 day'),
('TXN-2024-002', 2, 'PAYMENT', 250.50, 'USD', 'PROCESSING', 'GTW-12346', 7.52, null),
('TXN-2024-003', 3, 'PAYMENT', 75.99, 'USD', 'PENDING', null, 2.28, null),
('TXN-2024-004', 4, 'PAYMENT', 5000.00, 'USD', 'FAILED', 'GTW-12347', 0.00, CURRENT_TIMESTAMP - INTERVAL '2 hours'),
('TXN-2024-005', 5, 'PAYMENT', 199.99, 'USD', 'COMPLETED', 'GTW-12348', 5.99, CURRENT_TIMESTAMP - INTERVAL '3 hours');

-- Insert payment status history
INSERT INTO payment_status_history (payment_id, from_status, to_status, reason, changed_by, changed_at) VALUES
(1, 'PENDING', 'PROCESSING', 'Payment initiated', 'system', CURRENT_TIMESTAMP - INTERVAL '1 day 30 minutes'),
(1, 'PROCESSING', 'COMPLETED', 'Payment processed successfully', 'system', CURRENT_TIMESTAMP - INTERVAL '1 day'),
(2, 'PENDING', 'PROCESSING', 'Payment initiated', 'system', CURRENT_TIMESTAMP - INTERVAL '2 hours'),
(3, 'PENDING', 'PENDING', 'Awaiting payment method verification', 'system', CURRENT_TIMESTAMP - INTERVAL '1 hour'),
(4, 'PENDING', 'PROCESSING', 'Payment initiated', 'system', CURRENT_TIMESTAMP - INTERVAL '2 hours 30 minutes'),
(4, 'PROCESSING', 'FAILED', 'Insufficient funds', 'system', CURRENT_TIMESTAMP - INTERVAL '2 hours'),
(5, 'PENDING', 'PROCESSING', 'Payment initiated', 'system', CURRENT_TIMESTAMP - INTERVAL '3 hours 30 minutes'),
(5, 'PROCESSING', 'COMPLETED', 'Payment processed successfully', 'system', CURRENT_TIMESTAMP - INTERVAL '3 hours');

-- Insert sample API keys
INSERT INTO api_keys (key_id, key_hash, account_id, name, permissions) VALUES
('pk_test_12345', '$2a$12$API_KEY_HASH_1', 1, 'Enterprise Production API', '["payment.create", "payment.read", "payment.update"]'),
('pk_test_67890', '$2a$12$API_KEY_HASH_2', 2, 'TechStart Development API', '["payment.create", "payment.read"]'),
('pk_test_11111', '$2a$12$API_KEY_HASH_3', 3, 'Global Payments Webhook API', '["webhook.manage", "payment.read"]');

-- Insert sample webhooks
INSERT INTO webhooks (account_id, url, events, secret) VALUES
(1, 'https://enterprise.com/webhooks/payments', ARRAY['payment.completed', 'payment.failed'], 'whsec_enterprise_secret'),
(2, 'https://techstart.com/api/webhooks', ARRAY['payment.completed', 'payment.processing'], 'whsec_techstart_secret'),
(3, 'https://globalpay.com/webhook/handler', ARRAY['payment.completed', 'payment.failed', 'payment.refunded'], 'whsec_global_secret');

-- Insert sample audit logs
INSERT INTO audit_logs (entity_type, entity_id, action, new_values, correlation_id, user_id, ip_address) VALUES
('Payment', 1, 'CREATE', '{"amount": 1000.00, "status": "PENDING"}', 'corr-001', 1, '192.168.1.100'),
('Payment', 1, 'UPDATE', '{"status": "COMPLETED"}', 'corr-002', 1, '192.168.1.100'),
('Payment', 2, 'CREATE', '{"amount": 250.50, "status": "PENDING"}', 'corr-003', 2, '192.168.1.101'),
('User', 2, 'LOGIN', '{"last_login": "2024-01-15T10:30:00Z"}', 'corr-004', 2, '192.168.1.101'),
('Payment', 4, 'UPDATE', '{"status": "FAILED", "reason": "Insufficient funds"}', 'corr-005', 1, '192.168.1.100');