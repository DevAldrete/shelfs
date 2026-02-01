package com.devaldrete.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

  @Autowired
  private DataSource dataSource;

  @GetMapping("/health")
  public ResponseEntity<Map<String, Object>> health() {
    Map<String, Object> response = new HashMap<>();
    response.put("status", "UP");
    response.put("application", "Shelfs API");
    response.put("timestamp", LocalDateTime.now());
    response.put("version", "1.0.0");
    
    // Check database connectivity
    Map<String, String> database = new HashMap<>();
    try (Connection connection = dataSource.getConnection()) {
      database.put("status", "UP");
      database.put("database", connection.getMetaData().getDatabaseProductName());
      database.put("version", connection.getMetaData().getDatabaseProductVersion());
    } catch (Exception e) {
      database.put("status", "DOWN");
      database.put("error", e.getMessage());
    }
    response.put("database", database);
    
    return ResponseEntity.ok(response);
  }

  @GetMapping("/health/ready")
  public ResponseEntity<Map<String, String>> readiness() {
    Map<String, String> response = new HashMap<>();
    try (Connection connection = dataSource.getConnection()) {
      response.put("status", "READY");
      response.put("message", "Application is ready to accept requests");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      response.put("status", "NOT_READY");
      response.put("message", "Database connection failed");
      return ResponseEntity.status(503).body(response);
    }
  }

  @GetMapping("/health/live")
  public ResponseEntity<Map<String, String>> liveness() {
    Map<String, String> response = new HashMap<>();
    response.put("status", "ALIVE");
    response.put("message", "Application is running");
    return ResponseEntity.ok(response);
  }

  @GetMapping("/public/hello")
  public ResponseEntity<Map<String, String>> hello() {
    Map<String, String> response = new HashMap<>();
    response.put("message", "Hello from Shelfs API!");
    response.put("timestamp", LocalDateTime.now().toString());
    return ResponseEntity.ok(response);
  }
}
