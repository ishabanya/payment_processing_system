# KMS Key for Secrets Manager
resource "aws_kms_key" "secrets" {
  description             = "KMS key for Secrets Manager encryption"
  deletion_window_in_days = 7
  
  tags = merge(local.common_tags, {
    Name = "${local.name_prefix}-secrets-kms-key"
  })
}

resource "aws_kms_alias" "secrets" {
  name          = "alias/${local.name_prefix}-secrets"
  target_key_id = aws_kms_key.secrets.key_id
}

# Database Password Secret
resource "aws_secretsmanager_secret" "db_password" {
  name                    = "${local.name_prefix}/db/password"
  description             = "Database password for ${local.name_prefix}"
  kms_key_id              = aws_kms_key.secrets.arn
  recovery_window_in_days = 7
  
  tags = local.common_tags
}

resource "aws_secretsmanager_secret_version" "db_password" {
  secret_id     = aws_secretsmanager_secret.db_password.id
  secret_string = var.db_password
}

# JWT Secret
resource "aws_secretsmanager_secret" "jwt_secret" {
  name                    = "${local.name_prefix}/jwt/secret"
  description             = "JWT secret key for ${local.name_prefix}"
  kms_key_id              = aws_kms_key.secrets.arn
  recovery_window_in_days = 7
  
  tags = local.common_tags
}

resource "aws_secretsmanager_secret_version" "jwt_secret" {
  secret_id     = aws_secretsmanager_secret.jwt_secret.id
  secret_string = var.jwt_secret
}

# Redis Auth Token Secret
resource "aws_secretsmanager_secret" "redis_auth_token" {
  name                    = "${local.name_prefix}/redis/auth-token"
  description             = "Redis authentication token for ${local.name_prefix}"
  kms_key_id              = aws_kms_key.secrets.arn
  recovery_window_in_days = 7
  
  tags = local.common_tags
}

resource "aws_secretsmanager_secret_version" "redis_auth_token" {
  secret_id     = aws_secretsmanager_secret.redis_auth_token.id
  secret_string = var.redis_auth_token
}

# Application Configuration Secret (for additional configs)
resource "aws_secretsmanager_secret" "app_config" {
  name                    = "${local.name_prefix}/app/config"
  description             = "Application configuration for ${local.name_prefix}"
  kms_key_id              = aws_kms_key.secrets.arn
  recovery_window_in_days = 7
  
  tags = local.common_tags
}

resource "aws_secretsmanager_secret_version" "app_config" {
  secret_id = aws_secretsmanager_secret.app_config.id
  secret_string = jsonencode({
    encryption = {
      key = random_password.encryption_key.result
    }
    api_keys = {
      payment_gateway = random_password.payment_gateway_key.result
    }
    cors = {
      allowed_origins = var.domain_name != "" ? "https://${var.domain_name}" : "http://localhost:3000"
    }
  })
}

# Generate random passwords for application secrets
resource "random_password" "encryption_key" {
  length  = 32
  special = true
}

resource "random_password" "payment_gateway_key" {
  length  = 64
  special = false
}

# IAM Policy for ECS tasks to access secrets
resource "aws_iam_role_policy" "ecs_secrets_policy" {
  name = "${local.name_prefix}-ecs-secrets-policy"
  role = aws_iam_role.ecs_task_execution.id
  
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "secretsmanager:GetSecretValue"
        ]
        Resource = [
          aws_secretsmanager_secret.db_password.arn,
          aws_secretsmanager_secret.jwt_secret.arn,
          aws_secretsmanager_secret.redis_auth_token.arn,
          aws_secretsmanager_secret.app_config.arn
        ]
      },
      {
        Effect = "Allow"
        Action = [
          "kms:Decrypt"
        ]
        Resource = [
          aws_kms_key.secrets.arn
        ]
        Condition = {
          StringEquals = {
            "kms:ViaService" = "secretsmanager.${var.aws_region}.amazonaws.com"
          }
        }
      }
    ]
  })
}

# Parameter Store for non-sensitive configuration
resource "aws_ssm_parameter" "db_host" {
  name  = "/${local.name_prefix}/db/host"
  type  = "String"
  value = aws_db_instance.main.endpoint
  
  tags = local.common_tags
}

resource "aws_ssm_parameter" "db_name" {
  name  = "/${local.name_prefix}/db/name"
  type  = "String"
  value = var.db_name
  
  tags = local.common_tags
}

resource "aws_ssm_parameter" "db_username" {
  name  = "/${local.name_prefix}/db/username"
  type  = "String"
  value = var.db_username
  
  tags = local.common_tags
}

resource "aws_ssm_parameter" "redis_host" {
  name  = "/${local.name_prefix}/redis/host"
  type  = "String"
  value = aws_elasticache_replication_group.main.primary_endpoint_address
  
  tags = local.common_tags
}

resource "aws_ssm_parameter" "redis_port" {
  name  = "/${local.name_prefix}/redis/port"
  type  = "String"
  value = "6379"
  
  tags = local.common_tags
}

resource "aws_ssm_parameter" "environment" {
  name  = "/${local.name_prefix}/app/environment"
  type  = "String"
  value = var.environment
  
  tags = local.common_tags
}

resource "aws_ssm_parameter" "domain_name" {
  name  = "/${local.name_prefix}/app/domain"
  type  = "String"
  value = var.domain_name != "" ? var.domain_name : aws_lb.main.dns_name
  
  tags = local.common_tags
} 