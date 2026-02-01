# Quick Start Guide - Shelfs API

Get the Shelfs API up and running in under 2 minutes!

## Prerequisites

Choose ONE of the following:

### Option A: Docker (Easiest)
- Docker Desktop installed and running
- That's it!

### Option B: Local Development
- Java 17 or higher
- Maven 3.6+

## Getting Started

### 🚀 Method 1: One-Command Docker Setup (Recommended)

```bash
chmod +x docker-setup.sh && ./docker-setup.sh
```

That's it! The script will:
- ✅ Build the application
- ✅ Start PostgreSQL database
- ✅ Start the API server
- ✅ Run health checks
- ✅ Show you all the URLs

### 🛠️ Method 2: Using Make Commands

```bash
# See all available commands
make help

# Quick Docker setup
make docker-setup

# Or start development with H2 (in-memory database)
make dev
```

### 💻 Method 3: Local Development (No Docker)

```bash
# Run with H2 in-memory database
mvn spring-boot:run

# Or with PostgreSQL (start DB first)
make dev-db  # Starts PostgreSQL in Docker
make run     # Runs the app locally
```

## Verify It's Working

Once started, open these URLs in your browser:

### Health Check
http://localhost:8080/api/health

Expected response:
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

### Swagger UI (Interactive API Docs)
http://localhost:8080/swagger-ui.html

### Test the API

```bash
# Health check
curl http://localhost:8080/api/health

# Hello endpoint
curl http://localhost:8080/api/public/hello

# Create a user
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -u admin:admin \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'

# Get all users
curl http://localhost:8080/api/users -u admin:admin
```

## Default Credentials

- **Username**: `admin`
- **Password**: `admin`

⚠️ **Change these for production!**

## What's Next?

### 1. Explore the API
- Open Swagger UI: http://localhost:8080/swagger-ui.html
- Try out the endpoints interactively

### 2. View the Database
If using H2 (local development):
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:shelfsdb`
- Username: `sa`
- Password: (leave empty)

If using PostgreSQL with pgAdmin:
```bash
# Start pgAdmin
docker-compose --profile tools up -d

# Access at http://localhost:5050
# Email: admin@shelfs.com
# Password: admin
```

### 3. Check Logs

```bash
# Docker logs
docker-compose logs -f

# Or specific service
docker-compose logs -f api
```

### 4. Stop the Services

```bash
# Docker
docker-compose down

# Or with Make
make docker-down
```

## Troubleshooting

### Port 8080 already in use?

```bash
# Find what's using the port
lsof -i :8080

# Or change the port
docker-compose up -d -e SERVER_PORT=8081
```

### Docker not running?

```bash
# Check Docker status
docker info

# Start Docker Desktop and try again
```

### API not responding?

```bash
# Check service status
docker-compose ps

# View logs
docker-compose logs api

# Restart
docker-compose restart api
```

### Database connection failed?

```bash
# Check PostgreSQL
docker-compose ps postgres
docker-compose logs postgres

# Restart PostgreSQL
docker-compose restart postgres
```

### Clean slate?

```bash
# Remove everything and start fresh
docker-compose down -v
make clean
make docker-setup
```

## Common Commands

```bash
# Build the project
make build

# Run tests
make test

# View API health
make health

# Access database shell
make db-shell

# See all commands
make help
```

## Next Steps

1. ✅ API is running
2. 📚 Read the [README.md](README.md) for detailed documentation
3. 🐳 Check [DOCKER.md](DOCKER.md) for Docker details
4. 🔧 Start building your features!

## Need Help?

- Check the logs: `docker-compose logs -f`
- Review the configuration: `src/main/resources/application.yml`
- Verify health: `curl http://localhost:8080/api/health`

---

**Happy coding! 🚀**
