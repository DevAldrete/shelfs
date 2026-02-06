# Agent Guidelines for Shelfs Project

This document provides coding agents with essential information about the Shelfs library management system, including build commands, code style, and project conventions.

## Project Overview

- **Technology**: Java 17, Spring Boot 3.2.0, Maven
- **Architecture**: REST API with JPA, PostgreSQL (prod), H2 (dev)
- **Package Root**: `com.devaldrete`
- **Main Application**: `src/main/java/com/devaldrete/App.java`

## Build & Test Commands

### Development

```bash
# Run with H2 database (in-memory, no Docker required)
mvn spring-boot:run
# or
make dev

# Run with PostgreSQL (requires Docker)
make dev-db                           # Start PostgreSQL only
mvn spring-boot:run -Dspring-boot.run.profiles=prod
# or
make run
```

### Building

```bash
# Build JAR (skip tests)
mvn clean package -DskipTests
# or
make build

# Build with tests
mvn clean package
# or
make build-full

# Clean build artifacts
mvn clean
# or
make clean
```

### Testing

```bash
# Run all tests
mvn test
# or
make test

# Run a single test class
mvn test -Dtest=UserServiceTest

# Run a single test method
mvn test -Dtest=UserServiceTest#testGetUserById

# Run tests with coverage
mvn clean test jacoco:report
```

### Docker

```bash
# Quick setup (recommended for first time)
make docker-setup

# Manual Docker commands
make docker-build    # Build Docker image
make docker-up       # Start services
make docker-down     # Stop services
make docker-logs     # View logs
make docker-clean    # Clean up volumes
```

### Utilities

```bash
make health          # Check API health at http://localhost:8080/api/health
make db-shell        # Access PostgreSQL shell
make format          # Format code (if spotless configured)
```

## Project Structure

```
src/main/java/com/devaldrete/
├── App.java                    # Main Spring Boot application
├── config/                     # Configuration classes (Security, Web, etc.)
├── controller/                 # REST controllers (@RestController)
├── domain/                     # JPA entities (@Entity)
├── dto/                        # Data Transfer Objects
├── exception/                  # Custom exceptions and handlers
├── repository/                 # JPA repositories (@Repository)
└── service/                    # Business logic (@Service)

src/main/resources/
├── application.yml             # Development config (H2)
└── application-prod.yml        # Production config (PostgreSQL)

src/test/java/com/devaldrete/
└── *Test.java                  # JUnit tests
```

## Code Style Guidelines

### Package Organization

- **Controllers**: `com.devaldrete.controller` - REST endpoints
- **Services**: `com.devaldrete.service` - Business logic
- **Repositories**: `com.devaldrete.repository` - Data access
- **Entities**: `com.devaldrete.domain` - JPA entities
- **DTOs**: `com.devaldrete.dto` - Data transfer objects
- **Exceptions**: `com.devaldrete.exception` - Custom exceptions
- **Config**: `com.devaldrete.config` - Configuration classes

### Import Organization

1. Java standard library imports (`java.*`)
2. Jakarta EE imports (`jakarta.*`) - Note: Spring Boot 3.x uses Jakarta, not `javax.*`
3. Third-party library imports (alphabetical, e.g., `lombok.*`)
4. Spring framework imports (`org.springframework.*`)
5. Project imports (`com.devaldrete.*`)
6. Blank line between major groups

Example:

```java
package com.devaldrete.service;

import java.util.List;
import java.util.stream.Collectors;

import com.devaldrete.domain.User;
import com.devaldrete.dto.UserDTO;
import com.devaldrete.exception.ResourceNotFoundException;
import com.devaldrete.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
```

### Naming Conventions

- **Classes**: PascalCase (`UserService`, `BookController`)
- **Methods**: camelCase (`getUserById`, `createUser`)
- **Variables**: camelCase (`userRepository`, `userId`)
- **Constants**: UPPER_SNAKE_CASE (`MAX_LOGIN_ATTEMPTS`)
- **Packages**: lowercase (`controller`, `service`, `repository`)

### Entity Classes

- Use Lombok annotations: `@Getter`, `@Setter`, `@NoArgsConstructor`
- Place JPA annotations on fields
- Use `@Entity` and `@Table` annotations
- Example:

```java
@Entity
@Table(name = "users")
@Setter
@Getter
@NoArgsConstructor
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Column(nullable = false, unique = true)
  private String username;
}
```

### DTO Classes

- Use plain POJOs with explicit getters/setters (not Lombok)
- Add validation annotations: `@NotBlank`, `@Email`, `@Size`
- Include meaningful validation messages
- Example:

```java
public class UserDTO {
  @NotBlank(message = "Username is required")
  @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
  private String username;

  public String getUsername() { return username; }
  public void setUsername(String username) { this.username = username; }
}
```

### Service Classes

- Use constructor injection with `@Autowired`
- Mark with `@Service` and `@Transactional` annotations
- Follow single responsibility principle
- Example:

```java
@Service
@Transactional
public class UserService {
  private final UserRepository userRepository;

  @Autowired
  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }
}
```

### Controller Classes

- Use `@RestController` and `@RequestMapping` annotations
- Return `ResponseEntity<T>` for all endpoints
- Use appropriate HTTP status codes
- Validate request bodies with `@Valid`
- Example:

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
  @PostMapping
  public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
    UserDTO created = userService.createUser(userDTO);
    return new ResponseEntity<>(created, HttpStatus.CREATED);
  }
}
```

### Repository Interfaces

- Extend `JpaRepository<Entity, ID>`
- Use method naming conventions for queries
- Mark with `@Repository` annotation
- Example:

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);
  boolean existsByEmail(String email);
}
```

### Error Handling

- Create custom exceptions extending `RuntimeException`
- Use `@RestControllerAdvice` for global exception handling
- Return structured error responses with timestamps
- Example:

```java
public class ResourceNotFoundException extends RuntimeException {
  public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
    super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
  }
}
```

## API Endpoints

All endpoints are prefixed with `/api`:

- Health Check: `GET /api/health`
- Users: `/api/users` (CRUD operations)
- Books: `/api/books` (CRUD operations)
- Loans: `/api/loans` (CRUD operations)

## Environment & Configuration

### Profiles

- **Development**: Default profile (H2 database)
- **Production**: `prod` profile (PostgreSQL)

### Environment Variables

- `DB_USERNAME`: Database username (default: postgres)
- `DB_PASSWORD`: Database password (default: postgres)
- `SPRING_PROFILES_ACTIVE`: Active Spring profile (dev/prod)

### Key URLs (when running)

- API: <http://localhost:8080>
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- H2 Console: <http://localhost:8080/h2-console> (dev only)
- API Docs: <http://localhost:8080/api-docs>

## Important Notes

- Use constructor injection, not field injection
- Always validate DTOs with Jakarta Bean Validation
- Use `Optional<T>` for repository return types
- Encode passwords with `PasswordEncoder` before saving
- Follow RESTful conventions for endpoint design
- Write integration tests using `@SpringBootTest`
- Use meaningful exception messages with context
- Enable SQL logging in development for debugging
