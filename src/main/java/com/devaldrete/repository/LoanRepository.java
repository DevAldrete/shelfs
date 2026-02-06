package com.devaldrete.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.devaldrete.domain.BookItem;
import com.devaldrete.domain.Loan;
import com.devaldrete.domain.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing Loan entities.
 * Provides methods for querying loans by user, book item, status (active/returned),
 * and due date conditions.
 */
@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

  // ============================================
  // BASIC FINDERS
  // ============================================

  /**
   * Finds all loans for a specific user.
   *
   * @param user the user to find loans for
   * @return list of all loans for the user
   */
  List<Loan> findByUser(User user);

  /**
   * Finds all loans for a specific user by user ID.
   *
   * @param userId the ID of the user
   * @return list of all loans for the user
   */
  List<Loan> findByUserId(Long userId);

  /**
   * Finds all loans for a specific book item.
   *
   * @param bookItem the book item to find loans for
   * @return list of all loans for the book item
   */
  List<Loan> findByBookItem(BookItem bookItem);

  /**
   * Finds all loans for a specific book item by ID.
   *
   * @param bookItemId the ID of the book item
   * @return list of all loans for the book item
   */
  List<Loan> findByBookItemId(Long bookItemId);

  // ============================================
  // ACTIVE LOAN QUERIES (returnedAt IS NULL)
  // ============================================

  /**
   * Finds all active (not yet returned) loans for a specific user.
   *
   * @param user the user to find active loans for
   * @return list of active loans for the user
   */
  List<Loan> findByUserAndReturnedAtIsNull(User user);

  /**
   * Finds all active loans for a specific user by user ID.
   *
   * @param userId the ID of the user
   * @return list of active loans for the user
   */
  List<Loan> findByUserIdAndReturnedAtIsNull(Long userId);

  /**
   * Finds the active loan for a specific book item, if any.
   * A book item can only have one active loan at a time.
   *
   * @param bookItem the book item to find the active loan for
   * @return Optional containing the active loan if exists
   */
  Optional<Loan> findByBookItemAndReturnedAtIsNull(BookItem bookItem);

  /**
   * Finds the active loan for a specific book item by ID.
   *
   * @param bookItemId the ID of the book item
   * @return Optional containing the active loan if exists
   */
  Optional<Loan> findByBookItemIdAndReturnedAtIsNull(Long bookItemId);

  /**
   * Finds all active loans in the system.
   *
   * @return list of all loans that have not been returned
   */
  List<Loan> findByReturnedAtIsNull();

  /**
   * Checks if a book item currently has an active loan.
   * Used to validate before deleting or modifying book items.
   *
   * @param bookItem the book item to check
   * @return true if the book item has an active loan
   */
  boolean existsByBookItemAndReturnedAtIsNull(BookItem bookItem);

  /**
   * Checks if a book item (by ID) currently has an active loan.
   *
   * @param bookItemId the ID of the book item to check
   * @return true if the book item has an active loan
   */
  boolean existsByBookItemIdAndReturnedAtIsNull(Long bookItemId);

  /**
   * Counts the number of active loans for a specific user.
   *
   * @param user the user to count active loans for
   * @return the count of active loans
   */
  long countByUserAndReturnedAtIsNull(User user);

  /**
   * Counts the number of active loans for a specific user by ID.
   *
   * @param userId the ID of the user
   * @return the count of active loans
   */
  long countByUserIdAndReturnedAtIsNull(Long userId);

  // ============================================
  // RETURNED LOAN QUERIES (returnedAt IS NOT NULL)
  // ============================================

  /**
   * Finds all returned loans for a specific user.
   *
   * @param user the user to find returned loans for
   * @return list of returned loans for the user
   */
  List<Loan> findByUserAndReturnedAtIsNotNull(User user);

  /**
   * Finds all returned loans for a specific user by ID.
   *
   * @param userId the ID of the user
   * @return list of returned loans for the user
   */
  List<Loan> findByUserIdAndReturnedAtIsNotNull(Long userId);

  // ============================================
  // OVERDUE LOAN QUERIES
  // ============================================

  /**
   * Finds all overdue loans (active loans past their due date).
   *
   * @param currentTime the current time to compare against
   * @return list of overdue loans
   */
  @Query("SELECT l FROM Loan l WHERE l.returnedAt IS NULL AND l.limitAt < :currentTime")
  List<Loan> findOverdueLoans(@Param("currentTime") LocalDateTime currentTime);

  /**
   * Finds all overdue loans for a specific user.
   *
   * @param user the user to find overdue loans for
   * @param currentTime the current time to compare against
   * @return list of overdue loans for the user
   */
  @Query("SELECT l FROM Loan l WHERE l.user = :user AND l.returnedAt IS NULL AND l.limitAt < :currentTime")
  List<Loan> findOverdueLoansByUser(@Param("user") User user, @Param("currentTime") LocalDateTime currentTime);

  /**
   * Finds all overdue loans for a specific user by ID.
   *
   * @param userId the ID of the user
   * @param currentTime the current time to compare against
   * @return list of overdue loans for the user
   */
  @Query("SELECT l FROM Loan l WHERE l.user.id = :userId AND l.returnedAt IS NULL AND l.limitAt < :currentTime")
  List<Loan> findOverdueLoansByUserId(@Param("userId") Long userId, @Param("currentTime") LocalDateTime currentTime);

  /**
   * Counts all overdue loans in the system.
   *
   * @param currentTime the current time to compare against
   * @return the count of overdue loans
   */
  @Query("SELECT COUNT(l) FROM Loan l WHERE l.returnedAt IS NULL AND l.limitAt < :currentTime")
  long countOverdueLoans(@Param("currentTime") LocalDateTime currentTime);

  /**
   * Counts overdue loans for a specific user.
   *
   * @param user the user to count overdue loans for
   * @param currentTime the current time to compare against
   * @return the count of overdue loans
   */
  @Query("SELECT COUNT(l) FROM Loan l WHERE l.user = :user AND l.returnedAt IS NULL AND l.limitAt < :currentTime")
  long countOverdueLoansByUser(@Param("user") User user, @Param("currentTime") LocalDateTime currentTime);

  /**
   * Checks if a user has any overdue loans.
   *
   * @param user the user to check
   * @param currentTime the current time to compare against
   * @return true if the user has overdue loans
   */
  @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM Loan l " +
         "WHERE l.user = :user AND l.returnedAt IS NULL AND l.limitAt < :currentTime")
  boolean hasOverdueLoans(@Param("user") User user, @Param("currentTime") LocalDateTime currentTime);

  // ============================================
  // DUE SOON QUERIES
  // ============================================

  /**
   * Finds loans that are due soon (within a time window).
   * Useful for sending reminder notifications.
   *
   * @param startTime the start of the time window (usually now)
   * @param endTime the end of the time window (e.g., 3 days from now)
   * @return list of loans due within the time window
   */
  @Query("SELECT l FROM Loan l WHERE l.returnedAt IS NULL AND l.limitAt BETWEEN :startTime AND :endTime")
  List<Loan> findLoansDueBetween(
      @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime);

  /**
   * Finds loans due soon for a specific user.
   *
   * @param user the user to find loans for
   * @param startTime the start of the time window
   * @param endTime the end of the time window
   * @return list of loans due soon for the user
   */
  @Query("SELECT l FROM Loan l WHERE l.user = :user AND l.returnedAt IS NULL AND l.limitAt BETWEEN :startTime AND :endTime")
  List<Loan> findLoansDueBetweenForUser(
      @Param("user") User user,
      @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime);

  // ============================================
  // HISTORY AND STATISTICS
  // ============================================

  /**
   * Finds all loans created within a date range.
   *
   * @param startDate the start of the date range
   * @param endDate the end of the date range
   * @return list of loans created within the range
   */
  @Query("SELECT l FROM Loan l WHERE l.createdAt BETWEEN :startDate AND :endDate")
  List<Loan> findLoansCreatedBetween(
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  /**
   * Counts total loans for a specific user (both active and returned).
   *
   * @param userId the ID of the user
   * @return the total count of loans
   */
  long countByUserId(Long userId);

  /**
   * Finds the loan history for a specific book item, ordered by creation date descending.
   *
   * @param bookItemId the ID of the book item
   * @return list of loans ordered by most recent first
   */
  @Query("SELECT l FROM Loan l WHERE l.bookItem.id = :bookItemId ORDER BY l.createdAt DESC")
  List<Loan> findLoanHistoryByBookItemId(@Param("bookItemId") Long bookItemId);
}
