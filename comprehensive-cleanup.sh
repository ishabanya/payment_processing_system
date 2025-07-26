#!/bin/bash

set -e

echo "🧹 Comprehensive AWS Resource Cleanup for Payment System..."

AWS_REGION=${AWS_REGION:-us-east-1}
PROJECT_NAME=${PROJECT_NAME:-payment-system}
ENVIRONMENT=${ENVIRONMENT:-production}

# Function to safely delete a resource
safe_delete() {
    local resource_type="$1"
    local identifier="$2"
    local delete_command="$3"
    
    echo "Attempting to delete $resource_type: $identifier"
    if eval "$delete_command" 2>/dev/null; then
        echo "✅ Successfully deleted $resource_type: $identifier"
    else
        echo "⚠️ Could not delete $resource_type: $identifier (may not exist or in use)"
    fi
}

# Clean up any NAT Gateways that might be holding EIPs
echo "🚪 Cleaning up NAT Gateways..."
NAT_GATEWAYS=$(aws ec2 describe-nat-gateways --filter "Name=tag:Name,Values=${PROJECT_NAME}-${ENVIRONMENT}-*" --query 'NatGateways[?State==`available`].NatGatewayId' --output text 2>/dev/null || echo "")
for NAT_ID in $NAT_GATEWAYS; do
    echo "Deleting NAT Gateway: $NAT_ID"
    aws ec2 delete-nat-gateway --nat-gateway-id "$NAT_ID" 2>/dev/null || true
done

# Also clean up NAT gateways by subnet (more comprehensive)
SUBNETS=$(aws ec2 describe-subnets --filters "Name=tag:Project,Values=${PROJECT_NAME}" --query 'Subnets[].SubnetId' --output text 2>/dev/null || echo "")
for SUBNET_ID in $SUBNETS; do
    NAT_IN_SUBNET=$(aws ec2 describe-nat-gateways --filter "Name=subnet-id,Values=$SUBNET_ID" --query 'NatGateways[?State==`available`].NatGatewayId' --output text 2>/dev/null || echo "")
    for NAT_ID in $NAT_IN_SUBNET; do
        echo "Deleting NAT Gateway in subnet $SUBNET_ID: $NAT_ID"
        aws ec2 delete-nat-gateway --nat-gateway-id "$NAT_ID" 2>/dev/null || true
    done
done

# Wait for NAT gateways to be deleted
if [ ! -z "$NAT_GATEWAYS" ] || [ ! -z "$SUBNETS" ]; then
    echo "⏳ Waiting 60 seconds for NAT Gateways to be deleted..."
    sleep 60
fi

# Release unused Elastic IPs
echo "🌐 Cleaning up Elastic IPs..."
UNUSED_EIPS=$(aws ec2 describe-addresses --query 'Addresses[?AssociationId==null].AllocationId' --output text 2>/dev/null || echo "")
for EIP_ID in $UNUSED_EIPS; do
    echo "Releasing Elastic IP: $EIP_ID"
    aws ec2 release-address --allocation-id "$EIP_ID" 2>/dev/null || true
done

# Clean up Load Balancer and associated resources
echo "🔧 Cleaning up Load Balancer..."
ALB_ARN=$(aws elbv2 describe-load-balancers --names "${PROJECT_NAME}-${ENVIRONMENT}-alb" --query 'LoadBalancers[0].LoadBalancerArn' --output text 2>/dev/null || echo "")
if [ "$ALB_ARN" != "" ] && [ "$ALB_ARN" != "None" ]; then
    echo "Deleting Load Balancer: $ALB_ARN"
    aws elbv2 delete-load-balancer --load-balancer-arn "$ALB_ARN" 2>/dev/null || true
fi

# Clean up remaining RDS resources
echo "🗄️ Cleaning up RDS resources..."
aws rds delete-db-parameter-group --db-parameter-group-name "${PROJECT_NAME}-${ENVIRONMENT}-db-params" 2>/dev/null || true
aws rds delete-db-subnet-group --db-subnet-group-name "${PROJECT_NAME}-${ENVIRONMENT}-db-subnet-group" 2>/dev/null || true

# Clean up ElastiCache resources
echo "📊 Cleaning up ElastiCache resources..."
aws elasticache delete-cache-parameter-group --cache-parameter-group-name "${PROJECT_NAME}-${ENVIRONMENT}-redis-params" 2>/dev/null || true
aws elasticache delete-cache-subnet-group --cache-subnet-group-name "${PROJECT_NAME}-${ENVIRONMENT}-cache-subnet" 2>/dev/null || true

# Clean up IAM roles and policies
echo "🔐 Cleaning up IAM resources..."
for ROLE in "${PROJECT_NAME}-${ENVIRONMENT}-rds-monitoring-role" "${PROJECT_NAME}-${ENVIRONMENT}-ecs-task-execution-role" "${PROJECT_NAME}-${ENVIRONMENT}-ecs-task-role"; do
    echo "Cleaning up IAM role: $ROLE"
    
    # Detach policies first
    POLICIES=$(aws iam list-attached-role-policies --role-name "$ROLE" --query 'AttachedPolicies[].PolicyArn' --output text 2>/dev/null || echo "")
    for POLICY_ARN in $POLICIES; do
        aws iam detach-role-policy --role-name "$ROLE" --policy-arn "$POLICY_ARN" 2>/dev/null || true
    done
    
    # Delete inline policies
    INLINE_POLICIES=$(aws iam list-role-policies --role-name "$ROLE" --query 'PolicyNames[]' --output text 2>/dev/null || echo "")
    for POLICY_NAME in $INLINE_POLICIES; do
        aws iam delete-role-policy --role-name "$ROLE" --policy-name "$POLICY_NAME" 2>/dev/null || true
    done
    
    # Delete the role
    aws iam delete-role --role-name "$ROLE" 2>/dev/null || true
done

# Clean up WAF
echo "🛡️ Cleaning up WAF resources..."
WAF_ID=$(aws wafv2 list-web-acls --scope REGIONAL --query "WebACLs[?Name=='${PROJECT_NAME}-${ENVIRONMENT}-waf'].Id" --output text 2>/dev/null || echo "")
if [ "$WAF_ID" != "" ] && [ "$WAF_ID" != "None" ]; then
    LOCK_TOKEN=$(aws wafv2 get-web-acl --scope REGIONAL --id "$WAF_ID" --query 'LockToken' --output text 2>/dev/null || echo "")
    if [ "$LOCK_TOKEN" != "" ]; then
        echo "Deleting WAF WebACL: $WAF_ID"
        aws wafv2 delete-web-acl --scope REGIONAL --id "$WAF_ID" --lock-token "$LOCK_TOKEN" 2>/dev/null || true
    fi
fi

echo "✅ Comprehensive cleanup completed!"
echo "⏳ Waiting 30 seconds for AWS to propagate changes..."
sleep 30

echo "🚀 Ready for deployment!" 