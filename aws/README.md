# AWS Deployment Guide

Complete guide for deploying the Enterprise Payment Processing System to AWS using ECS Fargate, RDS, and ElastiCache.

## 🏗️ Architecture Overview

```
Internet → ALB → ECS Fargate → RDS PostgreSQL
                              ↓
                         ElastiCache Redis
```

### Components

- **ECS Fargate**: Serverless containers for backend and frontend
- **Application Load Balancer**: HTTPS termination and routing
- **RDS PostgreSQL**: Managed database with Multi-AZ
- **ElastiCache Redis**: Managed caching layer
- **VPC**: Isolated network with public/private subnets
- **WAF**: Web Application Firewall for security
- **CloudWatch**: Monitoring and logging
- **Secrets Manager**: Secure secret storage

## 📋 Prerequisites

### Required Tools
- AWS CLI v2.x
- Terraform v1.6+
- Docker v20.x+
- jq (JSON processor)

### AWS Requirements
- AWS Account with admin permissions
- Domain name (optional, for custom domain)
- SSL Certificate (optional, for HTTPS)

### Installation

#### AWS CLI
```bash
# macOS
brew install awscli

# Linux
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install
```

#### Terraform
```bash
# macOS
brew install terraform

# Linux
wget https://releases.hashicorp.com/terraform/1.6.0/terraform_1.6.0_linux_amd64.zip
unzip terraform_1.6.0_linux_amd64.zip
sudo mv terraform /usr/local/bin/
```

## 🚀 Quick Deployment

### Option 1: Automated Deployment
```bash
# Clone and navigate to AWS directory
cd aws

# Set environment variables
export AWS_PROFILE=shabanya
export ENVIRONMENT=production

# Run automated deployment
chmod +x deploy.sh
./deploy.sh all
```

### Option 2: Step-by-Step Deployment

#### 1. Configure AWS Credentials
```bash
aws configure
```

#### 2. Set Environment Variables
```bash
export AWS_REGION=us-east-1
export ENVIRONMENT=production
export PROJECT_NAME=payment-system

# Required secrets
export TF_VAR_db_password="your-strong-database-password"
export TF_VAR_jwt_secret="your-jwt-secret-key-32-chars-minimum"
export TF_VAR_redis_auth_token="your-redis-auth-token"

# Optional (for custom domain)
export TF_VAR_domain_name="payment.yourdomain.com"
export TF_VAR_certificate_arn="arn:aws:acm:us-east-1:123456789012:certificate/..."
```

#### 3. Deploy Infrastructure
```bash
cd terraform

# Initialize Terraform
terraform init

# Select or create workspace
terraform workspace select production || terraform workspace new production

# Plan deployment
terraform plan -var-file="../environments/production.tfvars" -out=tfplan

# Apply infrastructure
terraform apply tfplan

# Save outputs
terraform output -json > ../outputs/production-outputs.json
```

#### 4. Build and Push Docker Images
```bash
# Get ECR repository URLs
BACKEND_REPO=$(jq -r '.backend_ecr_repository_url.value' outputs/production-outputs.json)
FRONTEND_REPO=$(jq -r '.frontend_ecr_repository_url.value' outputs/production-outputs.json)

# Login to ECR
aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $BACKEND_REPO

# Build and push backend
docker build -f ../backend/Dockerfile.production -t $BACKEND_REPO:latest ../backend/
docker push $BACKEND_REPO:latest

# Build and push frontend
API_URL=$(jq -r '.api_url.value' outputs/production-outputs.json)
docker build --build-arg VITE_API_BASE_URL=$API_URL -f ../frontend/Dockerfile.production -t $FRONTEND_REPO:latest ../frontend/
docker push $FRONTEND_REPO:latest
```

#### 5. Deploy Services
```bash
# Update ECS services to use new images
CLUSTER_NAME=$(jq -r '.ecs_cluster_name.value' outputs/production-outputs.json)
BACKEND_SERVICE=$(jq -r '.backend_service_name.value' outputs/production-outputs.json)
FRONTEND_SERVICE=$(jq -r '.frontend_service_name.value' outputs/production-outputs.json)

# Force new deployment
aws ecs update-service --cluster $CLUSTER_NAME --service $BACKEND_SERVICE --force-new-deployment
aws ecs update-service --cluster $CLUSTER_NAME --service $FRONTEND_SERVICE --force-new-deployment

# Wait for stability
aws ecs wait services-stable --cluster $CLUSTER_NAME --services $BACKEND_SERVICE $FRONTEND_SERVICE
```

## 🔧 Configuration

### Environment Variables

Create `.env` files for each environment:

**Production (.env.production)**
```bash
# Database
DB_HOST=production-db-endpoint
DB_NAME=payment_system
DB_USERNAME=payment_user
DB_PASSWORD=stored-in-secrets-manager

# Redis
REDIS_HOST=production-redis-endpoint
REDIS_PORT=6379
REDIS_PASSWORD=stored-in-secrets-manager

# Application
JWT_SECRET=stored-in-secrets-manager
SPRING_PROFILES_ACTIVE=aws
ENVIRONMENT=production
```

### Secrets Management

All sensitive data is stored in AWS Secrets Manager:

- Database password: `payment-system-production/db/password`
- JWT secret: `payment-system-production/jwt/secret`
- Redis auth token: `payment-system-production/redis/auth-token`
- Application config: `payment-system-production/app/config`

## 📊 Monitoring & Observability

### CloudWatch Dashboards
- **Application Metrics**: CPU, Memory, Request counts
- **Database Metrics**: Connections, CPU, Storage
- **Load Balancer Metrics**: Response times, Error rates

### Logs
- Backend logs: `/ecs/payment-system-production/backend`
- Frontend logs: `/ecs/payment-system-production/frontend`
- WAF logs: `/aws/wafv2/payment-system-production`

### Alarms
- High CPU utilization (>90%)
- High memory usage (>90%)
- Database connection count (>150)
- ALB 5XX errors (>10)
- High response times (>2s)

### Viewing Logs
```bash
# Real-time backend logs
aws logs tail /ecs/payment-system-production/backend --follow

# Filter logs
aws logs filter-log-events \
  --log-group-name /ecs/payment-system-production/backend \
  --filter-pattern "ERROR" \
  --start-time $(date -d '1 hour ago' +%s)000
```

## 🔐 Security

### Network Security
- Private subnets for applications and databases
- Public subnets only for load balancers
- Security groups with minimal required access
- VPC endpoints for AWS services

### Application Security
- WAF with rate limiting and OWASP rules
- TLS 1.2+ encryption in transit
- Data encryption at rest (RDS, ElastiCache)
- Secrets stored in AWS Secrets Manager
- Non-root containers with security scanning

### Compliance
- PCI DSS compliant infrastructure
- Audit logging enabled
- Backup and recovery procedures
- Access controls and IAM roles

## 📈 Scaling

### Auto Scaling
- **Backend**: Scales 1-20 instances based on CPU/Memory/Request count
- **Frontend**: Scales 1-10 instances based on CPU/Memory
- **Database**: Read replicas for read scaling
- **Cache**: Multi-AZ Redis cluster

### Manual Scaling
```bash
# Scale backend to 5 instances
aws ecs update-service \
  --cluster payment-system-cluster \
  --service payment-system-production-backend \
  --desired-count 5

# Scale database vertically
aws rds modify-db-instance \
  --db-instance-identifier payment-system-production-db \
  --db-instance-class db.r6g.xlarge \
  --apply-immediately
```

## 🔄 CI/CD Pipeline

### GitHub Actions
The system includes a complete CI/CD pipeline:

1. **Test**: Run unit and integration tests
2. **Security Scan**: Vulnerability scanning with Trivy
3. **Build**: Create optimized Docker images
4. **Deploy**: Push to ECR and update ECS services
5. **Migrate**: Run database migrations
6. **Notify**: Send Slack notifications

### Required Secrets
Configure these in GitHub repository secrets:

```bash
AWS_ACCESS_KEY_ID=your-access-key
AWS_SECRET_ACCESS_KEY=your-secret-key
DB_PASSWORD=your-db-password
JWT_SECRET=your-jwt-secret
REDIS_AUTH_TOKEN=your-redis-token
DOMAIN_NAME=payment.yourdomain.com  # optional
CERTIFICATE_ARN=your-cert-arn       # optional
SLACK_WEBHOOK_URL=your-slack-hook   # optional
```

## 🛠️ Operations

### Health Checks
```bash
# Application health
curl https://payment.yourdomain.com/health

# API health
curl https://payment.yourdomain.com/api/v1/actuator/health

# Detailed health
curl https://payment.yourdomain.com/api/v1/actuator/health/liveness
curl https://payment.yourdomain.com/api/v1/actuator/health/readiness
```

### Database Operations
```bash
# Create database snapshot
aws rds create-db-snapshot \
  --db-instance-identifier payment-system-production-db \
  --db-snapshot-identifier payment-system-$(date +%Y%m%d%H%M%S)

# Connect to database (via bastion or VPN)
psql -h <db-endpoint> -U payment_user -d payment_system

# Run migrations manually
./deploy.sh migrate
```

### Troubleshooting

#### Service Won't Start
```bash
# Check service events
aws ecs describe-services \
  --cluster payment-system-cluster \
  --services payment-system-production-backend

# Check task logs
aws logs tail /ecs/payment-system-production/backend --follow
```

#### High Memory Usage
```bash
# Check current resource utilization
aws ecs describe-services \
  --cluster payment-system-cluster \
  --services payment-system-production-backend \
  --query 'services[0].deployments[0].taskDefinition'

# Update task definition with more memory
# Then redeploy service
```

#### Database Connection Issues
```bash
# Check database status
aws rds describe-db-instances \
  --db-instance-identifier payment-system-production-db

# Check security groups
aws ec2 describe-security-groups \
  --group-ids sg-your-rds-security-group
```

## 💰 Cost Optimization

### Estimated Monthly Costs

**Production Environment:**
- ECS Fargate: ~$200-400
- RDS (r6g.large): ~$300
- ElastiCache: ~$150
- ALB: ~$25
- Data Transfer: ~$50
- **Total: ~$725-925/month**

**Staging Environment:**
- ECS Fargate: ~$100-200
- RDS (t3.medium): ~$80
- ElastiCache: ~$50
- ALB: ~$25
- **Total: ~$255-355/month**

### Cost Reduction Tips
1. Use Spot instances for non-critical workloads
2. Schedule staging environment shutdown during off-hours
3. Enable S3 lifecycle policies for logs
4. Use Reserved Instances for predictable workloads
5. Monitor and optimize resource utilization

## 🗂️ Backup & Recovery

### Automated Backups
- **RDS**: Daily automated backups with 30-day retention
- **ElastiCache**: Daily snapshots with 3-day retention
- **Application**: Container images in ECR with lifecycle policies

### Disaster Recovery
1. **RTO**: < 15 minutes (automated failover)
2. **RPO**: < 5 minutes (transaction log backups)
3. **Multi-AZ**: Automatic failover for database
4. **Cross-Region**: Manual setup for DR region

### Recovery Procedures
```bash
# Restore database from snapshot
aws rds restore-db-instance-from-db-snapshot \
  --db-instance-identifier payment-system-restored \
  --db-snapshot-identifier payment-system-20231201120000

# Restore Redis from snapshot
aws elasticache create-cache-cluster \
  --cache-cluster-id payment-system-restored \
  --snapshot-name payment-system-redis-backup
```

## 📞 Support

### Monitoring Alerts
- Set up SNS topics for critical alerts
- Configure Slack/PagerDuty integration
- Monitor business metrics and SLAs

### Documentation
- [API Documentation](../docs/api/)
- [Architecture Decision Records](../docs/adr/)
- [Runbooks](../docs/runbooks/)

### Getting Help
1. Check CloudWatch logs and metrics
2. Review GitHub Issues and PRs
3. Consult AWS documentation
4. Contact DevOps team via Slack #devops

---

**🎉 Congratulations!** Your enterprise payment processing system is now running on AWS with enterprise-grade security, monitoring, and scalability. 