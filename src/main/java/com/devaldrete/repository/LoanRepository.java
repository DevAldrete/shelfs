package com.devaldrete.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.devaldrete.domain.BookItem;
import com.devaldrete.domain.Loan;
import com.devaldrete.domain.User;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
  List<Loan> findByUser(User user);

  // All loans for a specific book item
  List<Loan> findByBookItem(BookItem bookItem);

  // Active loans (not yet returned)
  List<Loan> findByUserAndReturnedAtIsNull();

  // All loans that are overdue
  List<Loan> findByLimitAtBefore(LocalDateTime dateTime);

  // Loans that are overdue
  List<Loan> findByUserAndLimitAtAfter(User user, LocalDateTime dateTime);

  // Loans that are active
  List<Loan> findByUserAndLimitAtBefore(User user, LocalDateTime dateTime);

  // Count of overdued loans
  int countByUserAndLimitAtBefore(User user, LocalDateTime dateTime);

  // Check existence of overdued loans
  boolean existsByUserAndLimitBefore(User user, LocalDateTime dateTime);

  // Find all active loans
  @Query("SELECT l FROM Loan l WHERE l.limitAt > :currentTime")
  List<Loan> findByAllActiveLoans(@Param("currentTime") LocalDateTime currentTime);

  // Loans that are due soon (e.g., within the next 3 days)
  @Query("SELECT l FROM Loan l WHERE l.returnedAt IS NULL AND l.limitAt BETWEEN :now AND :soonTime")
  List<Loan> findLoansDueSoon(@Param("now") LocalDateTime now, @Param("soonTime") LocalDateTime soonTime);
}
