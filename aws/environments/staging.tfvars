# Staging Environment Configuration
aws_region    = "us-east-1"
environment   = "staging"
project_name  = "payment-system"
vpc_cidr      = "10.1.0.0/16"

# ECS Configuration
ecs_cluster_name        = "payment-system-staging-cluster"
backend_desired_count   = 2
frontend_desired_count  = 1
backend_cpu            = 1024
backend_memory         = 2048
frontend_cpu           = 512  
frontend_memory        = 1024

# Database Configuration
db_instance_class              = "db.t3.medium"
db_allocated_storage          = 100
db_max_allocated_storage      = 500
db_name                       = "payment_system_staging"
db_username                   = "payment_user"
# db_password is set via environment variable or secrets
db_backup_retention_period    = 7
db_multi_az                   = false

# Redis Configuration
redis_node_type            = "cache.t3.medium"
redis_num_cache_nodes      = 2
redis_parameter_group_name = "default.redis7"
redis_port                 = 6379

# Security Configuration
# jwt_secret and redis_auth_token are set via environment variables or secrets

# Domain Configuration
# domain_name      = "staging.payment.yourdomain.com"
# certificate_arn  = "arn:aws:acm:us-east-1:123456789012:certificate/12345678-1234-1234-1234-123456789012"

# Monitoring Configuration
enable_cloudwatch_logs = true
log_retention_days     = 7

# Auto Scaling Configuration
backend_min_capacity  = 1
backend_max_capacity  = 5
frontend_min_capacity = 1
frontend_max_capacity = 3

# Health Check Configuration
health_check_grace_period = 300
health_check_interval     = 30
health_check_timeout      = 5
healthy_threshold         = 2
unhealthy_threshold       = 3

# Additional Tags
additional_tags = {
  Owner        = "DevOps Team"
  CostCenter   = "Engineering"
  Project      = "PaymentSystem"
  Environment  = "Staging"
  ManagedBy    = "Terraform"
} 