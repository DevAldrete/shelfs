package com.devaldrete.controller;

import java.util.List;

import com.devaldrete.dto.CreateLoanDTO;
import com.devaldrete.dto.LoanDTO;
import com.devaldrete.service.LoanService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing book loans.
 * Provides endpoints for creating, returning, and querying loans.
 * 
 * Typical library workflows supported:
 * 1. Look up user by email -> see their active loans
 * 2. Scan book barcode -> return book and close loan
 * 3. Create new loan for a user and book item
 */
@RestController
@RequestMapping("/api/loans")
public class LoanController {

  private final LoanService loanService;

  @Autowired
  public LoanController(LoanService loanService) {
    this.loanService = loanService;
  }

  // ============================================
  // LOAN CREATION
  // ============================================

  /**
   * Create a new loan for a user borrowing a book item.
   * Validates user eligibility and book availability.
   *
   * @param createLoanDTO the loan creation request
   * @return the created loan
   */
  @PostMapping
  public ResponseEntity<LoanDTO> createLoan(@Valid @RequestBody CreateLoanDTO createLoanDTO) {
    LoanDTO loan = loanService.createLoan(createLoanDTO);
    return new ResponseEntity<>(loan, HttpStatus.CREATED);
  }

  /**
   * Quick loan creation using just user ID and book item ID.
   * Uses default loan duration (14 days).
   *
   * @param userId the user ID
   * @param bookItemId the book item ID
   * @return the created loan
   */
  @PostMapping("/quick")
  public ResponseEntity<LoanDTO> createQuickLoan(
      @RequestParam Long userId,
      @RequestParam Long bookItemId) {
    LoanDTO loan = loanService.createLoan(userId, bookItemId);
    return new ResponseEntity<>(loan, HttpStatus.CREATED);
  }

  // ============================================
  // LOAN RETURN
  // ============================================

  /**
   * Return a loan by its ID.
   * Sets the book item status back to AVAILABLE.
   *
   * @param id the loan ID
   * @return the updated loan
   */
  @PostMapping("/{id}/return")
  public ResponseEntity<LoanDTO> returnLoan(@PathVariable Long id) {
    LoanDTO loan = loanService.returnLoan(id);
    return ResponseEntity.ok(loan);
  }

  /**
   * Return a book by scanning its barcode.
   * This is the primary workflow when books are returned to the library:
   * 1. Scan the book's barcode
   * 2. Find the active loan for that book
   * 3. Mark the loan as returned
   * 4. Set the book status back to AVAILABLE
   *
   * @param barcode the book item's barcode
   * @return the updated loan with user info
   */
  @PostMapping("/return/barcode/{barcode}")
  public ResponseEntity<LoanDTO> returnLoanByBarcode(@PathVariable String barcode) {
    LoanDTO loan = loanService.returnLoanByBarcode(barcode);
    return ResponseEntity.ok(loan);
  }

  // ============================================
  // LOAN RETRIEVAL
  // ============================================

  /**
   * Get all loans in the system.
   *
   * @return list of all loans
   */
  @GetMapping
  public ResponseEntity<List<LoanDTO>> getAllLoans() {
    List<LoanDTO> loans = loanService.getAllLoans();
    return ResponseEntity.ok(loans);
  }

  /**
   * Get a loan by its ID.
   *
   * @param id the loan ID
   * @return the loan
   */
  @GetMapping("/{id}")
  public ResponseEntity<LoanDTO> getLoanById(@PathVariable Long id) {
    LoanDTO loan = loanService.getLoanById(id);
    return ResponseEntity.ok(loan);
  }

  /**
   * Get all active (not returned) loans in the system.
   *
   * @return list of all active loans
   */
  @GetMapping("/active")
  public ResponseEntity<List<LoanDTO>> getAllActiveLoans() {
    List<LoanDTO> loans = loanService.getAllActiveLoans();
    return ResponseEntity.ok(loans);
  }

  // ============================================
  // USER-BASED LOAN QUERIES
  // ============================================

  /**
   * Get all loans for a specific user.
   *
   * @param userId the user ID
   * @return list of all loans for the user
   */
  @GetMapping("/user/{userId}")
  public ResponseEntity<List<LoanDTO>> getLoansByUserId(@PathVariable Long userId) {
    List<LoanDTO> loans = loanService.getLoansByUserId(userId);
    return ResponseEntity.ok(loans);
  }

  /**
   * Get all active loans for a specific user.
   *
   * @param userId the user ID
   * @return list of active loans for the user
   */
  @GetMapping("/user/{userId}/active")
  public ResponseEntity<List<LoanDTO>> getActiveLoansForUser(@PathVariable Long userId) {
    List<LoanDTO> loans = loanService.getActiveLoansForUser(userId);
    return ResponseEntity.ok(loans);
  }

  /**
   * Get loan history (returned loans) for a specific user.
   *
   * @param userId the user ID
   * @return list of returned loans for the user
   */
  @GetMapping("/user/{userId}/history")
  public ResponseEntity<List<LoanDTO>> getLoanHistoryForUser(@PathVariable Long userId) {
    List<LoanDTO> loans = loanService.getLoanHistoryForUser(userId);
    return ResponseEntity.ok(loans);
  }

  /**
   * Get all active loans for a user by their email address.
   * Primary workflow for library staff looking up a member's current loans.
   *
   * @param email the user's email address
   * @return list of active loans for the user
   */
  @GetMapping("/user/email/{email}/active")
  public ResponseEntity<List<LoanDTO>> getActiveLoansForUserByEmail(@PathVariable String email) {
    List<LoanDTO> loans = loanService.getActiveLoansForUserByEmail(email);
    return ResponseEntity.ok(loans);
  }

  /**
   * Get all loans for a user by their email address.
   *
   * @param email the user's email address
   * @return list of all loans for the user
   */
  @GetMapping("/user/email/{email}")
  public ResponseEntity<List<LoanDTO>> getLoansByUserEmail(@PathVariable String email) {
    List<LoanDTO> loans = loanService.getLoansByUserEmail(email);
    return ResponseEntity.ok(loans);
  }

  // ============================================
  // BOOK ITEM LOAN QUERIES
  // ============================================

  /**
   * Get the loan history for a specific book item.
   *
   * @param bookItemId the book item ID
   * @return list of loans for the book item
   */
  @GetMapping("/book-item/{bookItemId}/history")
  public ResponseEntity<List<LoanDTO>> getLoanHistoryForBookItem(@PathVariable Long bookItemId) {
    List<LoanDTO> loans = loanService.getLoanHistoryForBookItem(bookItemId);
    return ResponseEntity.ok(loans);
  }

  /**
   * Check if a book item is currently borrowed.
   *
   * @param bookItemId the book item ID
   * @return true if the book item has an active loan
   */
  @GetMapping("/book-item/{bookItemId}/borrowed")
  public ResponseEntity<Boolean> isBookItemBorrowed(@PathVariable Long bookItemId) {
    boolean borrowed = loanService.isBookItemBorrowed(bookItemId);
    return ResponseEntity.ok(borrowed);
  }

  // ============================================
  // OVERDUE LOAN OPERATIONS
  // ============================================

  /**
   * Get all overdue loans in the system.
   *
   * @return list of all overdue loans
   */
  @GetMapping("/overdue")
  public ResponseEntity<List<LoanDTO>> getOverdueLoans() {
    List<LoanDTO> loans = loanService.getOverdueLoans();
    return ResponseEntity.ok(loans);
  }

  /**
   * Get all overdue loans for a specific user.
   *
   * @param userId the user ID
   * @return list of overdue loans for the user
   */
  @GetMapping("/user/{userId}/overdue")
  public ResponseEntity<List<LoanDTO>> getOverdueLoansForUser(@PathVariable Long userId) {
    List<LoanDTO> loans = loanService.getOverdueLoansForUser(userId);
    return ResponseEntity.ok(loans);
  }

  /**
   * Check if a user has any overdue loans.
   *
   * @param userId the user ID
   * @return true if the user has overdue loans
   */
  @GetMapping("/user/{userId}/has-overdue")
  public ResponseEntity<Boolean> userHasOverdueLoans(@PathVariable Long userId) {
    boolean hasOverdue = loanService.userHasOverdueLoans(userId);
    return ResponseEntity.ok(hasOverdue);
  }

  /**
   * Get the count of overdue loans in the system.
   *
   * @return the count of overdue loans
   */
  @GetMapping("/overdue/count")
  public ResponseEntity<Long> countOverdueLoans() {
    long count = loanService.countOverdueLoans();
    return ResponseEntity.ok(count);
  }

  // ============================================
  // DUE SOON OPERATIONS
  // ============================================

  /**
   * Get loans that are due soon.
   * Default: loans due within 3 days.
   *
   * @param days number of days to look ahead (optional, default 3)
   * @return list of loans due within the specified days
   */
  @GetMapping("/due-soon")
  public ResponseEntity<List<LoanDTO>> getLoansDueSoon(
      @RequestParam(defaultValue = "3") int days) {
    List<LoanDTO> loans = loanService.getLoansDueSoon(days);
    return ResponseEntity.ok(loans);
  }

  /**
   * Get loans due soon for a specific user.
   *
   * @param userId the user ID
   * @param days number of days to look ahead (optional, default 3)
   * @return list of loans due soon for the user
   */
  @GetMapping("/user/{userId}/due-soon")
  public ResponseEntity<List<LoanDTO>> getLoansDueSoonForUser(
      @PathVariable Long userId,
      @RequestParam(defaultValue = "3") int days) {
    List<LoanDTO> loans = loanService.getLoansDueSoonForUser(userId, days);
    return ResponseEntity.ok(loans);
  }

  // ============================================
  // LOAN EXTENSION
  // ============================================

  /**
   * Extend a loan by the default duration (14 days).
   *
   * @param id the loan ID
   * @return the updated loan
   */
  @PostMapping("/{id}/extend")
  public ResponseEntity<LoanDTO> extendLoan(@PathVariable Long id) {
    LoanDTO loan = loanService.extendLoan(id);
    return ResponseEntity.ok(loan);
  }

  /**
   * Extend a loan by a specified number of days.
   *
   * @param id the loan ID
   * @param days the number of days to extend
   * @return the updated loan
   */
  @PostMapping("/{id}/extend/{days}")
  public ResponseEntity<LoanDTO> extendLoanByDays(
      @PathVariable Long id,
      @PathVariable int days) {
    LoanDTO loan = loanService.extendLoan(id, days);
    return ResponseEntity.ok(loan);
  }

  // ============================================
  // STATISTICS
  // ============================================

  /**
   * Get the count of active loans for a specific user.
   *
   * @param userId the user ID
   * @return the count of active loans
   */
  @GetMapping("/user/{userId}/active/count")
  public ResponseEntity<Long> countActiveLoansForUser(@PathVariable Long userId) {
    long count = loanService.countActiveLoansForUser(userId);
    return ResponseEntity.ok(count);
  }

  /**
   * Get the total count of loans for a specific user (active and returned).
   *
   * @param userId the user ID
   * @return the total count of loans
   */
  @GetMapping("/user/{userId}/count")
  public ResponseEntity<Long> countTotalLoansForUser(@PathVariable Long userId) {
    long count = loanService.countTotalLoansForUser(userId);
    return ResponseEntity.ok(count);
  }
}
