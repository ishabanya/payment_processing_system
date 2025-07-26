#!/bin/bash

set -e

echo "🧹 Cleaning up existing AWS resources..."

AWS_REGION=${AWS_REGION:-us-east-1}
PROJECT_NAME=${PROJECT_NAME:-payment-system}
ENVIRONMENT=${ENVIRONMENT:-production}

# Clean up RDS resources
echo "Cleaning RDS resources..."
aws rds delete-db-parameter-group --db-parameter-group-name "${PROJECT_NAME}-${ENVIRONMENT}-db-params" 2>/dev/null || true
aws rds delete-db-subnet-group --db-subnet-group-name "${PROJECT_NAME}-${ENVIRONMENT}-db-subnet-group" 2>/dev/null || true

# Clean up ElastiCache resources
echo "Cleaning ElastiCache resources..."
aws elasticache delete-cache-parameter-group --cache-parameter-group-name "${PROJECT_NAME}-${ENVIRONMENT}-redis-params" 2>/dev/null || true
aws elasticache delete-cache-subnet-group --cache-subnet-group-name "${PROJECT_NAME}-${ENVIRONMENT}-cache-subnet" 2>/dev/null || true

# Clean up ECR repositories
echo "Cleaning ECR repositories..."
aws ecr delete-repository --repository-name "${PROJECT_NAME}-${ENVIRONMENT}/backend" --force 2>/dev/null || true
aws ecr delete-repository --repository-name "${PROJECT_NAME}-${ENVIRONMENT}/frontend" --force 2>/dev/null || true

# Clean up Load Balancer
echo "Cleaning Load Balancer..."
ALB_ARN=$(aws elbv2 describe-load-balancers --names "${PROJECT_NAME}-${ENVIRONMENT}-alb" --query 'LoadBalancers[0].LoadBalancerArn' --output text 2>/dev/null) || true
if [ "$ALB_ARN" != "None" ] && [ "$ALB_ARN" != "" ]; then
    aws elbv2 delete-load-balancer --load-balancer-arn "$ALB_ARN" 2>/dev/null || true
fi

# Clean up IAM roles
echo "Cleaning IAM roles..."
aws iam delete-role --role-name "${PROJECT_NAME}-${ENVIRONMENT}-rds-monitoring-role" 2>/dev/null || true
aws iam delete-role --role-name "${PROJECT_NAME}-${ENVIRONMENT}-ecs-task-execution-role" 2>/dev/null || true
aws iam delete-role --role-name "${PROJECT_NAME}-${ENVIRONMENT}-ecs-task-role" 2>/dev/null || true

# Clean up KMS aliases
echo "Cleaning KMS aliases..."
aws kms delete-alias --alias-name "alias/${PROJECT_NAME}-${ENVIRONMENT}-rds" 2>/dev/null || true
aws kms delete-alias --alias-name "alias/${PROJECT_NAME}-${ENVIRONMENT}-redis" 2>/dev/null || true
aws kms delete-alias --alias-name "alias/${PROJECT_NAME}-${ENVIRONMENT}-secrets" 2>/dev/null || true

# Clean up CloudWatch Log Groups
echo "Cleaning CloudWatch Log Groups..."
aws logs delete-log-group --log-group-name "/aws/elasticache/${PROJECT_NAME}-${ENVIRONMENT}/redis/slow-log" 2>/dev/null || true
aws logs delete-log-group --log-group-name "/ecs/${PROJECT_NAME}-${ENVIRONMENT}/backend" 2>/dev/null || true
aws logs delete-log-group --log-group-name "/ecs/${PROJECT_NAME}-${ENVIRONMENT}/frontend" 2>/dev/null || true
aws logs delete-log-group --log-group-name "/aws/wafv2/${PROJECT_NAME}-${ENVIRONMENT}" 2>/dev/null || true

# Clean up Secrets Manager secrets
echo "Cleaning Secrets Manager secrets..."
aws secretsmanager delete-secret --secret-id "${PROJECT_NAME}-${ENVIRONMENT}/db/password" --force-delete-without-recovery 2>/dev/null || true
aws secretsmanager delete-secret --secret-id "${PROJECT_NAME}-${ENVIRONMENT}/jwt/secret" --force-delete-without-recovery 2>/dev/null || true
aws secretsmanager delete-secret --secret-id "${PROJECT_NAME}-${ENVIRONMENT}/redis/auth-token" --force-delete-without-recovery 2>/dev/null || true
aws secretsmanager delete-secret --secret-id "${PROJECT_NAME}-${ENVIRONMENT}/app/config" --force-delete-without-recovery 2>/dev/null || true

# Clean up SSM Parameters
echo "Cleaning SSM Parameters..."
aws ssm delete-parameter --name "/${PROJECT_NAME}-${ENVIRONMENT}/db/name" 2>/dev/null || true
aws ssm delete-parameter --name "/${PROJECT_NAME}-${ENVIRONMENT}/db/username" 2>/dev/null || true
aws ssm delete-parameter --name "/${PROJECT_NAME}-${ENVIRONMENT}/redis/port" 2>/dev/null || true
aws ssm delete-parameter --name "/${PROJECT_NAME}-${ENVIRONMENT}/app/environment" 2>/dev/null || true

# Clean up WAF
echo "Cleaning WAF resources..."
WAF_ID=$(aws wafv2 list-web-acls --scope REGIONAL --query "WebACLs[?Name=='${PROJECT_NAME}-${ENVIRONMENT}-waf'].Id" --output text 2>/dev/null) || true
if [ "$WAF_ID" != "" ] && [ "$WAF_ID" != "None" ]; then
    aws wafv2 delete-web-acl --scope REGIONAL --id "$WAF_ID" --lock-token "$(aws wafv2 get-web-acl --scope REGIONAL --id "$WAF_ID" --query 'LockToken' --output text 2>/dev/null)" 2>/dev/null || true
fi

echo "✅ Cleanup completed. Waiting 30 seconds for AWS to propagate changes..."
sleep 30

echo "🚀 Ready for fresh deployment!" 