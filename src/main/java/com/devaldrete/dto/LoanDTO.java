package com.devaldrete.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Loan entity.
 * Contains all loan information including references to user and book item.
 */
public class LoanDTO {

  private Long id;
  private Long userId;
  private String username;
  private Long bookItemId;
  private String bookItemBarcode;
  private String bookTitle;
  private LocalDateTime createdAt;
  private LocalDateTime limitAt;
  private LocalDateTime returnedAt;
  private boolean active;
  private boolean overdue;

  // Default constructor
  public LoanDTO() {
  }

  // Full constructor
  public LoanDTO(Long id, Long userId, String username, Long bookItemId,
                 String bookItemBarcode, String bookTitle, LocalDateTime createdAt,
                 LocalDateTime limitAt, LocalDateTime returnedAt) {
    this.id = id;
    this.userId = userId;
    this.username = username;
    this.bookItemId = bookItemId;
    this.bookItemBarcode = bookItemBarcode;
    this.bookTitle = bookTitle;
    this.createdAt = createdAt;
    this.limitAt = limitAt;
    this.returnedAt = returnedAt;
    this.active = returnedAt == null;
    this.overdue = this.active && LocalDateTime.now().isAfter(limitAt);
  }

  // Getters and Setters

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public Long getBookItemId() {
    return bookItemId;
  }

  public void setBookItemId(Long bookItemId) {
    this.bookItemId = bookItemId;
  }

  public String getBookItemBarcode() {
    return bookItemBarcode;
  }

  public void setBookItemBarcode(String bookItemBarcode) {
    this.bookItemBarcode = bookItemBarcode;
  }

  public String getBookTitle() {
    return bookTitle;
  }

  public void setBookTitle(String bookTitle) {
    this.bookTitle = bookTitle;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getLimitAt() {
    return limitAt;
  }

  public void setLimitAt(LocalDateTime limitAt) {
    this.limitAt = limitAt;
  }

  public LocalDateTime getReturnedAt() {
    return returnedAt;
  }

  public void setReturnedAt(LocalDateTime returnedAt) {
    this.returnedAt = returnedAt;
    this.active = returnedAt == null;
    updateOverdueStatus();
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public boolean isOverdue() {
    return overdue;
  }

  public void setOverdue(boolean overdue) {
    this.overdue = overdue;
  }

  /**
   * Updates the overdue status based on current time.
   * Should be called when displaying the DTO to ensure accurate status.
   */
  public void updateOverdueStatus() {
    this.overdue = this.active && limitAt != null && LocalDateTime.now().isAfter(limitAt);
  }
}
