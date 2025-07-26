#!/bin/bash

# AWS Deployment Environment Setup
# Run this script after configuring AWS credentials: aws configure

echo "🔧 Setting up AWS deployment environment variables..."

# Generate secure secrets if not provided
generate_secret() {
    openssl rand -base64 32 | tr -d "=+/" | cut -c1-32
}

# Set required environment variables
export AWS_REGION=us-east-1
export ENVIRONMENT=production
export PROJECT_NAME=payment-system

# Generate or set secrets (modify these with your own values)
export TF_VAR_db_password="${TF_VAR_db_password:-$(generate_secret)}"
export TF_VAR_jwt_secret="${TF_VAR_jwt_secret:-$(generate_secret)}"
export TF_VAR_redis_auth_token="${TF_VAR_redis_auth_token:-$(generate_secret)}"

# Optional domain configuration (uncomment if you have a domain)
# export TF_VAR_domain_name="payment.yourdomain.com"
# export TF_VAR_certificate_arn="arn:aws:acm:us-east-1:123456789012:certificate/..."

echo "✅ Environment variables set:"
echo "   AWS_REGION: $AWS_REGION"
echo "   ENVIRONMENT: $ENVIRONMENT"
echo "   PROJECT_NAME: $PROJECT_NAME"
echo "   AWS_PROFILE: $AWS_PROFILE"
echo ""
echo "🔐 Generated secrets (save these securely):"
echo "   DB Password: $TF_VAR_db_password"
echo "   JWT Secret: $TF_VAR_jwt_secret"
echo "   Redis Token: $TF_VAR_redis_auth_token"
echo ""
echo "🚀 Ready to run deployment: ./deploy.sh all" 