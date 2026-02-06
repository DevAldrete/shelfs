package com.devaldrete.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Handles ResourceNotFoundException.
   * Returns HTTP 404 Not Found with error details.
   */
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
      ResourceNotFoundException ex, WebRequest request) {
    ErrorResponse errorResponse = new ErrorResponse(
        HttpStatus.NOT_FOUND.value(),
        ex.getMessage(),
        LocalDateTime.now(),
        request.getDescription(false)
    );
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  /**
   * Handles BookItemInLoanException.
   * Returns HTTP 409 Conflict when attempting to delete a borrowed book item.
   */
  @ExceptionHandler(BookItemInLoanException.class)
  public ResponseEntity<ErrorResponse> handleBookItemInLoanException(
      BookItemInLoanException ex, WebRequest request) {
    ErrorResponse errorResponse = new ErrorResponse(
        HttpStatus.CONFLICT.value(),
        ex.getMessage(),
        LocalDateTime.now(),
        request.getDescription(false)
    );
    return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    Map<String, Object> response = new HashMap<>();
    Map<String, String> errors = new HashMap<>();
    
    ex.getBindingResult().getAllErrors().forEach((error) -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });
    
    response.put("status", HttpStatus.BAD_REQUEST.value());
    response.put("errors", errors);
    response.put("timestamp", LocalDateTime.now());
    
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGlobalException(
      Exception ex, WebRequest request) {
    ErrorResponse errorResponse = new ErrorResponse(
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "An unexpected error occurred: " + ex.getMessage(),
        LocalDateTime.now(),
        request.getDescription(false)
    );
    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
