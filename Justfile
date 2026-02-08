# Shelfs API - Available Commands
# Run `just` or `just --list` to see this help

set dotenv-load    # optional: loads .env if you have one

# Default – shows help when you just run `just`
default:
    @just --list

# ────────────────────────────────────────────────
# Development
# ────────────────────────────────────────────────

dev:
    @echo "Starting in development mode (H2)..."
    mvn spring-boot:run

dev-db:
    @echo "Starting PostgreSQL for development..."
    docker compose -f docker-compose.dev.yml up -d
    @echo "PostgreSQL ready at localhost:5432"

run:
    @echo "Running with PostgreSQL..."
    @export SPRING_PROFILES_ACTIVE=prod && mvn spring-boot:run

# ────────────────────────────────────────────────
# Docker
# ────────────────────────────────────────────────

docker-setup:
    @chmod +x docker-setup.sh
    @./docker-setup.sh

docker-build:
    @echo "Building Docker image..."
    docker compose build

docker-up:
    @echo "Starting Docker services..."
    docker compose up -d
    @echo "Services starting... Check logs with 'just docker-logs'"

docker-down:
    @echo "Stopping Docker services..."
    docker compose down

docker-logs:
    docker compose logs -f

docker-clean:
    @echo "Cleaning Docker services and volumes..."
    docker compose down -v
    @echo "Cleaning Docker images..."
    docker system prune -f

docker-restart:
    @echo "Restarting Docker services..."
    docker compose restart

# ────────────────────────────────────────────────
# Build & Test
# ────────────────────────────────────────────────

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

# ────────────────────────────────────────────────
# Utilities
# ────────────────────────────────────────────────

health:
    @echo "Checking API health..."
    @curl -s http://localhost:8080/api/health | python3 -m json.tool || echo "API not responding"

db-shell:
    @echo "Connecting to PostgreSQL..."
    docker compose exec postgres psql -U shelfsuser -d shelfsdb

format:
    @echo "Formatting code..."
    mvn spotless:apply

# ────────────────────────────────────────────────
# Production
# ────────────────────────────────────────────────

prod:
    @echo "Building and running in production mode..."
    mvn clean package -DskipTests
    java -jar target/shelfs-1.0-SNAPSHOT.jar --spring.profiles.active=prod
