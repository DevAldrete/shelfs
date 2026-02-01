.PHONY: help build run test clean docker-build docker-up docker-down docker-logs docker-clean dev prod

# Default target
help:
	@echo "Shelfs API - Available Commands"
	@echo "================================"
	@echo ""
	@echo "Development:"
	@echo "  make dev              - Run with H2 database (no Docker)"
	@echo "  make dev-db           - Start PostgreSQL only (Docker)"
	@echo "  make run              - Run locally with PostgreSQL"
	@echo ""
	@echo "Docker:"
	@echo "  make docker-setup     - Quick Docker setup (build + run + health check)"
	@echo "  make docker-build     - Build Docker image"
	@echo "  make docker-up        - Start Docker services"
	@echo "  make docker-down      - Stop Docker services"
	@echo "  make docker-logs      - View Docker logs"
	@echo "  make docker-clean     - Stop and remove volumes"
	@echo "  make docker-restart   - Restart Docker services"
	@echo ""
	@echo "Build & Test:"
	@echo "  make build            - Build JAR (skip tests)"
	@echo "  make build-full       - Build JAR with tests"
	@echo "  make test             - Run tests"
	@echo "  make clean            - Clean build artifacts"
	@echo ""
	@echo "Utilities:"
	@echo "  make health           - Check API health"
	@echo "  make db-shell         - Access PostgreSQL shell"
	@echo "  make format           - Format code"
	@echo ""

# Development
dev:
	@echo "Starting in development mode (H2)..."
	mvn spring-boot:run

dev-db:
	@echo "Starting PostgreSQL for development..."
	docker-compose -f docker-compose.dev.yml up -d
	@echo "PostgreSQL ready at localhost:5432"

run:
	@echo "Running with PostgreSQL..."
	@export SPRING_PROFILES_ACTIVE=prod && mvn spring-boot:run

# Docker commands
docker-setup:
	@chmod +x docker-setup.sh
	@./docker-setup.sh

docker-build:
	@echo "Building Docker image..."
	docker-compose build

docker-up:
	@echo "Starting Docker services..."
	docker-compose up -d
	@echo "Services starting... Check logs with 'make docker-logs'"

docker-down:
	@echo "Stopping Docker services..."
	docker-compose down

docker-logs:
	docker-compose logs -f

docker-clean:
	@echo "Cleaning Docker services and volumes..."
	docker-compose down -v
	@echo "Cleaning Docker images..."
	docker system prune -f

docker-restart:
	@echo "Restarting Docker services..."
	docker-compose restart

# Build & Test
build:
	@echo "Building project (skipping tests)..."
	mvn clean package -DskipTests

build-full:
	@echo "Building project with tests..."
	mvn clean package

test:
	@echo "Running tests..."
	mvn test

clean:
	@echo "Cleaning build artifacts..."
	mvn clean

# Utilities
health:
	@echo "Checking API health..."
	@curl -s http://localhost:8080/api/health | python3 -m json.tool || echo "API not responding"

db-shell:
	@echo "Connecting to PostgreSQL..."
	docker-compose exec postgres psql -U shelfsuser -d shelfsdb

format:
	@echo "Formatting code..."
	mvn spotless:apply

# Production
prod:
	@echo "Building and running in production mode..."
	mvn clean package -DskipTests
	java -jar target/shelfs-1.0-SNAPSHOT.jar --spring.profiles.active=prod
