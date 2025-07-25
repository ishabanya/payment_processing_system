#!/bin/bash

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
AWS_REGION=${AWS_REGION:-us-east-1}
ENVIRONMENT=${ENVIRONMENT:-production}
PROJECT_NAME=${PROJECT_NAME:-payment-system}

echo -e "${BLUE}🚀 AWS Payment Processing System Deployment${NC}"
echo -e "${BLUE}============================================${NC}"

# Check prerequisites
check_prerequisites() {
    echo -e "${YELLOW}📋 Checking prerequisites...${NC}"
    
    if ! command -v aws &> /dev/null; then
        echo -e "${RED}❌ AWS CLI is not installed${NC}"
        exit 1
    fi
    
    if ! command -v terraform &> /dev/null; then
        echo -e "${RED}❌ Terraform is not installed${NC}"
        exit 1
    fi
    
    if ! command -v docker &> /dev/null; then
        echo -e "${RED}❌ Docker is not installed${NC}"
        exit 1
    fi
    
    if ! command -v jq &> /dev/null; then
        echo -e "${RED}❌ jq is not installed${NC}"
        exit 1
    fi
    
    # Check AWS credentials
    if ! aws sts get-caller-identity &> /dev/null; then
        echo -e "${RED}❌ AWS credentials not configured${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✅ All prerequisites met${NC}"
}

# Setup Terraform backend
setup_terraform_backend() {
    echo -e "${YELLOW}🗄️ Setting up Terraform backend...${NC}"
    
    local bucket_name="${PROJECT_NAME}-terraform-state-$(date +%s)"
    local dynamodb_table="${PROJECT_NAME}-terraform-locks"
    
    # Create S3 bucket for state
    if ! aws s3api head-bucket --bucket "$bucket_name" 2>/dev/null; then
        echo "Creating S3 bucket for Terraform state..."
        aws s3api create-bucket \
            --bucket "$bucket_name" \
            --region "$AWS_REGION" \
            --create-bucket-configuration LocationConstraint="$AWS_REGION"
        
        # Enable versioning
        aws s3api put-bucket-versioning \
            --bucket "$bucket_name" \
            --versioning-configuration Status=Enabled
        
        # Enable encryption
        aws s3api put-bucket-encryption \
            --bucket "$bucket_name" \
            --server-side-encryption-configuration '{
                "Rules": [{
                    "ApplyServerSideEncryptionByDefault": {
                        "SSEAlgorithm": "AES256"
                    }
                }]
            }'
        
        # Block public access
        aws s3api put-public-access-block \
            --bucket "$bucket_name" \
            --public-access-block-configuration BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true
    fi
    
    # Create DynamoDB table for locks
    if ! aws dynamodb describe-table --table-name "$dynamodb_table" &>/dev/null; then
        echo "Creating DynamoDB table for Terraform locks..."
        aws dynamodb create-table \
            --table-name "$dynamodb_table" \
            --attribute-definitions AttributeName=LockID,AttributeType=S \
            --key-schema AttributeName=LockID,KeyType=HASH \
            --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5
        
        # Wait for table to be created
        aws dynamodb wait table-exists --table-name "$dynamodb_table"
    fi
    
    # Update Terraform backend configuration
    cat > terraform/backend.tf << EOF
terraform {
  backend "s3" {
    bucket         = "$bucket_name"
    key            = "$ENVIRONMENT/terraform.tfstate"
    region         = "$AWS_REGION"
    dynamodb_table = "$dynamodb_table"
    encrypt        = true
  }
}
EOF
    
    echo -e "${GREEN}✅ Terraform backend configured${NC}"
}

# Deploy infrastructure
deploy_infrastructure() {
    echo -e "${YELLOW}🏗️ Deploying infrastructure...${NC}"
    
    cd terraform
    
    # Initialize Terraform
    terraform init
    
    # Validate configuration
    terraform validate
    
    # Format check
    terraform fmt -check
    
    # Create workspace if it doesn't exist
    terraform workspace select "$ENVIRONMENT" 2>/dev/null || terraform workspace new "$ENVIRONMENT"
    
    # Plan deployment
    echo "Creating deployment plan..."
    terraform plan \
        -var-file="../environments/${ENVIRONMENT}.tfvars" \
        -out=tfplan
    
    # Apply infrastructure
    echo "Applying infrastructure changes..."
    terraform apply -auto-approve tfplan
    
    # Get outputs
    terraform output -json > "../outputs/${ENVIRONMENT}-outputs.json"
    
    cd ..
    
    echo -e "${GREEN}✅ Infrastructure deployed successfully${NC}"
}

# Build and push Docker images
build_and_push_images() {
    echo -e "${YELLOW}🐳 Building and pushing Docker images...${NC}"
    
    # Get ECR repository URLs from Terraform outputs
    local backend_repo=$(jq -r '.backend_ecr_repository_url.value' "outputs/${ENVIRONMENT}-outputs.json")
    local frontend_repo=$(jq -r '.frontend_ecr_repository_url.value' "outputs/${ENVIRONMENT}-outputs.json")
    
    # Login to ECR
    aws ecr get-login-password --region "$AWS_REGION" | docker login --username AWS --password-stdin "${backend_repo%/*}"
    
    # Build and push backend
    echo "Building backend image..."
    docker build -f ../backend/Dockerfile.production -t "$backend_repo:latest" ../backend/
    docker push "$backend_repo:latest"
    
    # Build and push frontend
    echo "Building frontend image..."
    local api_url=$(jq -r '.api_url.value' "outputs/${ENVIRONMENT}-outputs.json")
    docker build \
        --build-arg VITE_API_BASE_URL="$api_url" \
        -f ../frontend/Dockerfile.production \
        -t "$frontend_repo:latest" \
        ../frontend/
    docker push "$frontend_repo:latest"
    
    echo -e "${GREEN}✅ Docker images built and pushed${NC}"
}

# Deploy application
deploy_application() {
    echo -e "${YELLOW}🚀 Deploying application...${NC}"
    
    local cluster_name=$(jq -r '.ecs_cluster_name.value' "outputs/${ENVIRONMENT}-outputs.json")
    local backend_service=$(jq -r '.backend_service_name.value' "outputs/${ENVIRONMENT}-outputs.json")
    local frontend_service=$(jq -r '.frontend_service_name.value' "outputs/${ENVIRONMENT}-outputs.json")
    
    # Update backend service
    echo "Updating backend service..."
    aws ecs update-service \
        --cluster "$cluster_name" \
        --service "$backend_service" \
        --force-new-deployment \
        --region "$AWS_REGION" > /dev/null
    
    # Update frontend service
    echo "Updating frontend service..."
    aws ecs update-service \
        --cluster "$cluster_name" \
        --service "$frontend_service" \
        --force-new-deployment \
        --region "$AWS_REGION" > /dev/null
    
    # Wait for services to stabilize
    echo "Waiting for services to stabilize..."
    aws ecs wait services-stable \
        --cluster "$cluster_name" \
        --services "$backend_service" \
        --region "$AWS_REGION"
    
    aws ecs wait services-stable \
        --cluster "$cluster_name" \
        --services "$frontend_service" \
        --region "$AWS_REGION"
    
    echo -e "${GREEN}✅ Application deployed successfully${NC}"
}

# Run database migrations
run_migrations() {
    echo -e "${YELLOW}📊 Running database migrations...${NC}"
    
    local cluster_name=$(jq -r '.ecs_cluster_name.value' "outputs/${ENVIRONMENT}-outputs.json")
    local subnet_ids=$(jq -r '.private_subnet_ids.value[]' "outputs/${ENVIRONMENT}-outputs.json" | head -1)
    local security_group=$(jq -r '.backend_security_group_id.value' "outputs/${ENVIRONMENT}-outputs.json")
    
    # Create migration task definition
    local task_def='{
        "family": "'$PROJECT_NAME-$ENVIRONMENT'-migration",
        "networkMode": "awsvpc",
        "requiresCompatibilities": ["FARGATE"],
        "cpu": "256",
        "memory": "512",
        "executionRoleArn": "'$(jq -r '.ecs_task_execution_role_arn.value' "outputs/${ENVIRONMENT}-outputs.json")'",
        "containerDefinitions": [{
            "name": "migration",
            "image": "'$(jq -r '.backend_ecr_repository_url.value' "outputs/${ENVIRONMENT}-outputs.json")':latest",
            "essential": true,
            "command": ["java", "-jar", "app.jar", "--spring.flyway.clean-disabled=false", "--spring.flyway.migrate"],
            "environment": [
                {"name": "SPRING_PROFILES_ACTIVE", "value": "aws,migration"},
                {"name": "DB_HOST", "value": "'$(jq -r '.database_endpoint.value' "outputs/${ENVIRONMENT}-outputs.json")'"},
                {"name": "DB_NAME", "value": "'$(jq -r '.database_name.value' "outputs/${ENVIRONMENT}-outputs.json")'"},
                {"name": "DB_USERNAME", "value": "'$(jq -r '.database_username.value' "outputs/${ENVIRONMENT}-outputs.json")'"}
            ],
            "secrets": [
                {"name": "DB_PASSWORD", "valueFrom": "'$(jq -r '.db_password_secret_arn.value' "outputs/${ENVIRONMENT}-outputs.json")'"}
            ],
            "logConfiguration": {
                "logDriver": "awslogs",
                "options": {
                    "awslogs-group": "/ecs/'$PROJECT_NAME-$ENVIRONMENT'/migration",
                    "awslogs-region": "'$AWS_REGION'",
                    "awslogs-stream-prefix": "ecs"
                }
            }
        }]
    }'
    
    # Register migration task definition
    local task_def_arn=$(echo "$task_def" | aws ecs register-task-definition \
        --cli-input-json file:///dev/stdin \
        --query 'taskDefinition.taskDefinitionArn' \
        --output text)
    
    # Run migration task
    local task_arn=$(aws ecs run-task \
        --cluster "$cluster_name" \
        --task-definition "$task_def_arn" \
        --launch-type FARGATE \
        --network-configuration "awsvpcConfiguration={subnets=[$subnet_ids],securityGroups=[$security_group],assignPublicIp=DISABLED}" \
        --query 'tasks[0].taskArn' \
        --output text)
    
    # Wait for migration to complete
    echo "Waiting for migration to complete..."
    aws ecs wait tasks-stopped \
        --cluster "$cluster_name" \
        --tasks "$task_arn"
    
    # Check if migration succeeded
    local exit_code=$(aws ecs describe-tasks \
        --cluster "$cluster_name" \
        --tasks "$task_arn" \
        --query 'tasks[0].containers[0].exitCode' \
        --output text)
    
    if [ "$exit_code" != "0" ]; then
        echo -e "${RED}❌ Database migration failed${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✅ Database migrations completed${NC}"
}

# Show deployment status
show_status() {
    echo -e "${BLUE}📊 Deployment Status${NC}"
    echo -e "${BLUE}==================${NC}"
    
    local app_url=$(jq -r '.application_url.value' "outputs/${ENVIRONMENT}-outputs.json")
    local api_url=$(jq -r '.api_url.value' "outputs/${ENVIRONMENT}-outputs.json")
    local dashboard_url=$(jq -r '.cloudwatch_dashboard_url.value' "outputs/${ENVIRONMENT}-outputs.json")
    
    echo -e "${GREEN}🌐 Application URL: ${app_url}${NC}"
    echo -e "${GREEN}🔧 API URL: ${api_url}${NC}"
    echo -e "${GREEN}📈 Dashboard: ${dashboard_url}${NC}"
    
    echo -e "\n${YELLOW}🏥 Health Checks:${NC}"
    echo -e "Frontend: ${app_url}/health"
    echo -e "Backend: ${api_url}/actuator/health"
    
    echo -e "\n${YELLOW}📱 Quick Commands:${NC}"
    echo -e "View logs: aws logs tail /ecs/${PROJECT_NAME}-${ENVIRONMENT}/backend --follow"
    echo -e "Scale backend: aws ecs update-service --cluster ${PROJECT_NAME}-cluster --service ${PROJECT_NAME}-${ENVIRONMENT}-backend --desired-count 3"
    echo -e "Connect to DB: aws rds describe-db-instances --db-instance-identifier ${PROJECT_NAME}-${ENVIRONMENT}-db"
}

# Main deployment function
main() {
    case "${1:-all}" in
        "prerequisites")
            check_prerequisites
            ;;
        "backend")
            setup_terraform_backend
            ;;
        "infrastructure")
            deploy_infrastructure
            ;;
        "images")
            build_and_push_images
            ;;
        "application")
            deploy_application
            ;;
        "migrate")
            run_migrations
            ;;
        "status")
            show_status
            ;;
        "all")
            check_prerequisites
            setup_terraform_backend
            deploy_infrastructure
            build_and_push_images
            deploy_application
            run_migrations
            show_status
            ;;
        *)
            echo "Usage: $0 {prerequisites|backend|infrastructure|images|application|migrate|status|all}"
            echo ""
            echo "Commands:"
            echo "  prerequisites  - Check required tools and credentials"
            echo "  backend       - Setup Terraform S3 backend"
            echo "  infrastructure - Deploy AWS infrastructure with Terraform"
            echo "  images        - Build and push Docker images to ECR"
            echo "  application   - Deploy application to ECS"
            echo "  migrate       - Run database migrations"
            echo "  status        - Show deployment status and URLs"
            echo "  all           - Run full deployment (default)"
            exit 1
            ;;
    esac
}

# Create required directories
mkdir -p outputs environments

main "$@" 