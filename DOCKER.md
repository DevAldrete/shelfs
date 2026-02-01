# Docker Setup for Shelfs API

This directory contains Docker configuration for running the Shelfs API.

## Quick Start

### Option 1: Using the Setup Script (Recommended)

```bash
# Make the script executable
chmod +x docker-setup.sh

# Run the setup
./docker-setup.sh
```

### Option 2: Manual Docker Compose

```bash
# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

## Services

### Production Setup (`docker-compose.yml`)

Includes:

- **PostgreSQL**: Production database on port 5432
- **Shelfs API**: Spring Boot application on port 8080
- **pgAdmin** (optional): Database management UI on port 5050

To include pgAdmin:

```bash
docker-compose --profile tools up -d
```

### Development Setup (`docker-compose.dev.yml`)

Minimal setup for development with just PostgreSQL:

```bash
docker-compose -f docker-compose.dev.yml up -d
```

Then run the Spring Boot app locally:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## Available Endpoints

Once running, access:

- **API Health**: <http://localhost:8080/api/health>
- **Liveness Probe**: <http://localhost:8080/api/health/live>
- **Readiness Probe**: <http://localhost:8080/api/health/ready>
- **Swagger UI**: <http://localhost:8080/swagger-ui.html>
- **pgAdmin** (if enabled): <http://localhost:5050>

## Database Access

### PostgreSQL Connection Details

- **Host**: localhost
- **Port**: 5432
- **Database**: shelfsdb
- **Username**: shelfsuser
- **Password**: shelfspass

### pgAdmin Access

- **URL**: <http://localhost:5050>
- **Email**: <admin@shelfs.com>
- **Password**: admin

To connect pgAdmin to PostgreSQL:

1. Open pgAdmin
2. Right-click "Servers" → "Register" → "Server"
3. General tab: Name = "Shelfs DB"
4. Connection tab:
   - Host: postgres (not localhost!)
   - Port: 5432
   - Database: shelfsdb
   - Username: shelfsuser
   - Password: shelfspass

## Docker Commands

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f api
docker-compose logs -f postgres
```

### Restart Services

```bash
# Restart all
docker-compose restart

# Restart specific service
docker-compose restart api
```

### Stop Services

```bash
# Stop without removing
docker-compose stop

# Stop and remove containers
docker-compose down

# Stop and remove volumes (DELETES DATA!)
docker-compose down -v
```

### Rebuild

```bash
# Rebuild API image
docker-compose build api

# Rebuild and restart
docker-compose up -d --build
```

### Execute Commands in Containers

```bash
# Access PostgreSQL shell
docker-compose exec postgres psql -U shelfsuser -d shelfsdb

# Access API container
docker-compose exec api sh

# View API logs inside container
docker-compose exec api cat /app/logs/application.log
```

## Health Checks

The setup includes health checks for all services:

### PostgreSQL Health

```bash
docker-compose exec postgres pg_isready -U shelfsuser
```

### API Health

```bash
curl http://localhost:8080/api/health
```

Detailed health response:

```json
{
  "status": "UP",
  "application": "Shelfs API",
  "timestamp": "2026-01-16T...",
  "version": "1.0.0",
  "database": {
    "status": "UP",
    "database": "PostgreSQL",
    "version": "16.x"
  }
}
```

## Environment Variables

You can override default settings by creating a `.env` file:

```env
# Database
POSTGRES_DB=shelfsdb
POSTGRES_USER=shelfsuser
POSTGRES_PASSWORD=shelfspass

# API
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod

# pgAdmin
PGADMIN_EMAIL=admin@shelfs.com
PGADMIN_PASSWORD=admin
```

Then use it with:

```bash
docker-compose --env-file .env up -d
```

## Troubleshooting

### Port Already in Use

```bash
# Check what's using port 8080
lsof -i :8080

# Or use different port
docker-compose up -d -e SERVER_PORT=8081
```

### Database Connection Issues

```bash
# Check if PostgreSQL is running
docker-compose ps postgres

# View PostgreSQL logs
docker-compose logs postgres

# Restart PostgreSQL
docker-compose restart postgres
```

### API Won't Start

```bash
# Check API logs
docker-compose logs api

# Rebuild API image
docker-compose build --no-cache api
docker-compose up -d api
```

### Clean Slate

```bash
# Remove everything and start fresh
docker-compose down -v
docker system prune -a
./docker-setup.sh
```

## Production Considerations

For production deployment:

1. **Change default passwords** in docker-compose.yml
2. **Use Docker secrets** instead of environment variables
3. **Add reverse proxy** (nginx/traefik) with SSL
4. **Configure persistent volumes** with proper backup strategy
5. **Set resource limits** for containers
6. **Use production-grade logging** (ELK stack, Splunk, etc.)
7. **Enable monitoring** (Prometheus, Grafana)
8. **Use Docker Swarm** or Kubernetes for orchestration

## Multi-Stage Build

The Dockerfile uses multi-stage builds to:

- Reduce final image size
- Separate build and runtime dependencies
- Run as non-root user for security
- Include health checks

Image sizes:

- Build stage: ~600MB (discarded)
- Final image: ~200MB
