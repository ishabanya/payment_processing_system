# Production Environment Configuration
aws_region    = "us-east-1"
environment   = "production"
project_name  = "payment-system"
vpc_cidr      = "10.0.0.0/16"

# ECS Configuration
ecs_cluster_name        = "payment-system-cluster"
backend_desired_count   = 3
frontend_desired_count  = 2
backend_cpu            = 2048
backend_memory         = 4096
frontend_cpu           = 1024  
frontend_memory        = 2048

# Database Configuration
db_instance_class              = "db.r6g.large"
db_allocated_storage          = 200
db_max_allocated_storage      = 2000
db_name                       = "payment_system"
db_username                   = "payment_user"
# db_password is set via environment variable or secrets
db_backup_retention_period    = 30
db_multi_az                   = true

# Redis Configuration
redis_node_type            = "cache.r6g.large"
redis_num_cache_nodes      = 3
redis_parameter_group_name = "default.redis7"
redis_port                 = 6379

# Security Configuration
# jwt_secret and redis_auth_token are set via environment variables or secrets

# Domain Configuration
# domain_name      = "payment.yourdomain.com"
# certificate_arn  = "arn:aws:acm:us-east-1:123456789012:certificate/12345678-1234-1234-1234-123456789012"

# Monitoring Configuration
enable_cloudwatch_logs = true
log_retention_days     = 30

# Auto Scaling Configuration
backend_min_capacity  = 2
backend_max_capacity  = 20
frontend_min_capacity = 1
frontend_max_capacity = 10

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
  Environment  = "Production"
  ManagedBy    = "Terraform"
  Compliance   = "PCI-DSS"
} 