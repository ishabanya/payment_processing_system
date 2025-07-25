-- Test data for unit and integration tests

-- Insert test accounts
INSERT INTO accounts (id, account_number, account_name, email, phone, balance, currency_code, created_by) VALUES
(1, 'TEST000001', 'Test Enterprise Corp', 'test@enterprise.com', '+1-555-0001', 10000.00, 'USD', 'system'),
(2, 'TEST000002', 'Test TechStart Inc', 'test@techstart.com', '+1-555-0002', 5000.00, 'USD', 'system'),
(3, 'TEST000003', 'Test Global Payments Ltd', 'test@globalpay.com', '+1-555-0003', 15000.00, 'USD', 'system');

-- Insert test users (password is 'password123' hashed with BCrypt)
INSERT INTO users (id, username, email, password_hash, first_name, last_name, role, account_id, is_active) VALUES
(1, 'testadmin', 'test@enterprise.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/lewdBcQu8ZCW5nMV2', 'Test', 'Admin', 'ADMIN', 1, true),
(2, 'testmerchant', 'test@techstart.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/lewdBcQu8ZCW5nMV2', 'Test', 'Merchant', 'MERCHANT', 2, true),
(3, 'testuser', 'test@globalpay.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/lewdBcQu8ZCW5nMV2', 'Test', 'User', 'USER', 3, true);

-- Insert test payment methods
INSERT INTO payment_methods (id, account_id, type, provider, encrypted_details, last_four_digits, expiry_month, expiry_year, is_default, is_active) VALUES
(1, 1, 'CREDIT_CARD', 'Visa', 'encrypted_test_card_1', '1234', 12, 2025, true, true),
(2, 2, 'CREDIT_CARD', 'MasterCard', 'encrypted_test_card_2', '5678', 10, 2024, true, true),
(3, 3, 'DIGITAL_WALLET', 'PayPal', 'encrypted_test_wallet_1', null, null, null, true, true);

-- Insert test payments
INSERT INTO payments (id, payment_reference, account_id, payment_method_id, amount, currency_code, description, status, merchant_reference) VALUES
(1, 'TEST-PAY-001', 1, 1, 100.00, 'USD', 'Test Payment 1', 'COMPLETED', 'TEST-ORD-001'),
(2, 'TEST-PAY-002', 2, 2, 250.50, 'USD', 'Test Payment 2', 'PENDING', 'TEST-ORD-002'),
(3, 'TEST-PAY-003', 3, 3, 75.99, 'USD', 'Test Payment 3', 'FAILED', 'TEST-ORD-003');

-- Insert test transactions
INSERT INTO transactions (id, transaction_reference, payment_id, type, amount, currency_code, status, processing_fee) VALUES
(1, 'TEST-TXN-001', 1, 'PAYMENT', 100.00, 'USD', 'COMPLETED', 2.90),
(2, 'TEST-TXN-002', 2, 'PAYMENT', 250.50, 'USD', 'PENDING', 7.25),
(3, 'TEST-TXN-003', 3, 'PAYMENT', 75.99, 'USD', 'FAILED', 0.00);

-- Reset sequences for H2 database
ALTER SEQUENCE accounts_id_seq RESTART WITH 4;
ALTER SEQUENCE users_id_seq RESTART WITH 4;
ALTER SEQUENCE payment_methods_id_seq RESTART WITH 4;
ALTER SEQUENCE payments_id_seq RESTART WITH 4;
ALTER SEQUENCE transactions_id_seq RESTART WITH 4;