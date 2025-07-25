#!/bin/bash

echo "🚀 Launching Enterprise Payment Processing System..."

# Check prerequisites
check_prereq() {
    command -v docker >/dev/null 2>&1 || { echo "❌ Docker is required but not installed."; exit 1; }
    command -v docker-compose >/dev/null 2>&1 || { echo "❌ Docker Compose is required but not installed."; exit 1; }
    command -v node >/dev/null 2>&1 || { echo "❌ Node.js is required but not installed."; exit 1; }
    command -v mvn >/dev/null 2>&1 || { echo "❌ Maven is required but not installed."; exit 1; }
}

# Launch database and infrastructure
launch_infrastructure() {
    echo "📦 Starting infrastructure services..."
    docker-compose -f docker/docker-compose.yml up -d postgres redis rabbitmq elasticsearch prometheus grafana
    
    echo "⏳ Waiting for services to be ready..."
    sleep 30
}

# Launch backend
launch_backend() {
    echo "🔧 Starting backend service..."
    cd backend
    
    # Set environment variables
    export SPRING_PROFILES_ACTIVE=dev
    export DB_HOST=localhost
    export DB_PORT=5432
    export DB_NAME=payment_system
    export DB_USERNAME=payment_user
    export DB_PASSWORD=payment_pass
    export REDIS_HOST=localhost
    export REDIS_PORT=6379
    export REDIS_PASSWORD=payment_redis_pass
    
    # Compile and run
    mvn clean compile -DskipTests
    mvn spring-boot:run &
    BACKEND_PID=$!
    
    cd ..
    echo "✅ Backend started with PID: $BACKEND_PID"
}

# Launch frontend
launch_frontend() {
    echo "🎨 Starting frontend service..."
    cd frontend
    
    # Install dependencies and start
    npm install
    VITE_API_BASE_URL=http://localhost:8080/api/v1 npm run dev &
    FRONTEND_PID=$!
    
    cd ..
    echo "✅ Frontend started with PID: $FRONTEND_PID"
}

# Main execution
main() {
    check_prereq
    launch_infrastructure
    launch_backend
    launch_frontend
    
    echo ""
    echo "🎉 Enterprise Payment Processing System is launching!"
    echo ""
    echo "📱 Frontend:      http://localhost:3000"
    echo "🔧 Backend API:   http://localhost:8080"
    echo "📊 Grafana:       http://localhost:3001 (admin/admin123)"
    echo "📈 Prometheus:    http://localhost:9090"
    echo "📋 Kibana:        http://localhost:5601"
    echo "🐰 RabbitMQ:      http://localhost:15672 (payment_rabbit/payment_rabbit_pass)"
    echo ""
    echo "⏳ Services are starting up... Please wait 2-3 minutes for full initialization."
    echo ""
    echo "To stop services: docker-compose -f docker/docker-compose.yml down"
    echo "To view logs: docker-compose -f docker/docker-compose.yml logs -f [service_name]"
}

# Trap to cleanup on exit
trap 'echo "🛑 Stopping services..."; docker-compose -f docker/docker-compose.yml down' EXIT

main "$@"