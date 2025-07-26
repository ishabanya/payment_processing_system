-- Create payment system database if it doesn't exist
-- Note: The database 'payment_system' is already created by environment variables

-- Create extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Grant permissions to the payment_user
GRANT ALL PRIVILEGES ON DATABASE payment_system TO payment_user;
GRANT ALL PRIVILEGES ON SCHEMA public TO payment_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO payment_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO payment_user;

-- Set timezone
SET timezone = 'UTC'; 