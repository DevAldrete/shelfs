# Shelfs

## What is Shelfs?

A library system designed for flexibility while being robust and easy to use.

## Spring Boot API Setup

### Tech Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA** - Database ORM
- **Spring Security** - Authentication & Authorization
- **H2 Database** - Development (in-memory)
- **PostgreSQL** - Production
- **Lombok** - Reduce boilerplate
- **MapStruct** - DTO mapping
- **SpringDoc OpenAPI** - API documentation (Swagger)
- **Maven** - Build tool
- **Docker** - Containerization

### Quick Start

#### Option 1: Docker (Recommended)

The easiest way to get started:

```bash
# Make the script executable
chmod +x docker-setup.sh

# Run the automated setup
./docker-setup.sh
```

This will:
- Build the Docker image
- Start PostgreSQL database
- Start the Spring Boot API
- Run health checks
- Display all service URLs

Manual Docker commands:
```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

See [DOCKER.md](DOCKER.md) for detailed Docker documentation.

#### Option 2: Local Development (H2 Database)

```bash
mvn spring-boot:run
```

#### Option 3: Local Development (PostgreSQL)

```bash
# Start PostgreSQL with Docker
docker-compose -f docker-compose.dev.yml up -d

# Run the application locally
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### API Endpoints

Once running, access:

- **Health Check**: http://localhost:8080/api/health
- **Liveness**: http://localhost:8080/api/health/live
- **Readiness**: http://localhost:8080/api/health/ready
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs
- **H2 Console**: http://localhost:8080/h2-console (dev only)

### Default Credentials (Development)
- **Username**: `admin`
- **Password**: `admin`

### Project Structure

```
src/main/java/com/devaldrete/
├── App.java                          # Main application class
├── config/                           # Configuration classes
├── controller/                       # REST controllers
├── domain/                          # Entity models
├── dto/                             # Data transfer objects
├── exception/                       # Exception handling
├── repository/                      # Data repositories
└── service/                         # Business logic
```

### Building for Production

```bash
# Build JAR
mvn clean package

# Run JAR
java -jar target/shelfs-1.0-SNAPSHOT.jar

# Build Docker image
docker build -t shelfs-api .

# Run Docker container
docker run -p 8080:8080 shelfs-api
```

See the full documentation in the code for more details.
