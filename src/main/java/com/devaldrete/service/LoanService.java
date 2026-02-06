package com.devaldrete.service;

import java.time.LocalDateTime;
import java.util.List;

import com.devaldrete.domain.BookItem;
import com.devaldrete.domain.BookStatus;
import com.devaldrete.domain.Loan;
import com.devaldrete.domain.User;
import com.devaldrete.dto.CreateLoanDTO;
import com.devaldrete.dto.LoanDTO;
import com.devaldrete.exception.ResourceNotFoundException;
import com.devaldrete.repository.BookItemRepository;
import com.devaldrete.repository.LoanRepository;
import com.devaldrete.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for managing loans.
 * Provides business logic for creating, returning, and querying book loans.
 * Handles validation of book availability and user eligibility.
 */
@Service
@Transactional
public class LoanService {

  private final LoanRepository loanRepository;
  private final UserRepository userRepository;
  private final BookItemRepository bookItemRepository;

  /**
   * Default loan duration in days when no specific limit is provided.
   */
  private static final int DEFAULT_LOAN_DURATION_DAYS = 14;

  /**
   * Maximum number of active loans a user can have at once.
   */
  private static final int MAX_ACTIVE_LOANS_PER_USER = 5;

  @Autowired
  public LoanService(
      LoanRepository loanRepository,
      UserRepository userRepository,
      BookItemRepository bookItemRepository) {
    this.loanRepository = loanRepository;
    this.userRepository = userRepository;
    this.bookItemRepository = bookItemRepository;
  }

  // ============================================
  // LOAN CREATION
  // ============================================

  /**
   * Creates a new loan for a user borrowing a book item.
   * Validates that:
   * - The user exists and is eligible to borrow
   * - The book item exists and is available
   * - The user hasn't exceeded maximum active loans
   * - The user doesn't have overdue books
   *
   * @param createLoanDTO the loan creation request containing userId, bookItemId,
   *                      and limitAt
   * @return the created loan as a DTO
   * @throws ResourceNotFoundException if user or book item not found
   * @throws IllegalStateException     if validation fails
   */
  public LoanDTO createLoan(CreateLoanDTO createLoanDTO) {
    User user = findUserOrThrow(createLoanDTO.getUserId());
    BookItem bookItem = findBookItemOrThrow(createLoanDTO.getBookItemId());

    validateUserCanBorrow(user);
    validateBookItemAvailable(bookItem);

    Loan loan = new Loan();
    loan.setUser(user);
    loan.setBookItem(bookItem);
    loan.setCreatedAt(LocalDateTime.now());
    loan.setLimitAt(createLoanDTO.getLimitAt() != null
        ? createLoanDTO.getLimitAt()
        : LocalDateTime.now().plusDays(DEFAULT_LOAN_DURATION_DAYS));

    // Update book item status to BORROWED
    bookItem.setStatus(BookStatus.BORROWED);
    bookItemRepository.save(bookItem);

    Loan savedLoan = loanRepository.save(loan);
    return convertToDTO(savedLoan);
  }

  /**
   * Creates a loan with default duration.
   *
   * @param userId     the ID of the user borrowing the book
   * @param bookItemId the ID of the book item being borrowed
   * @return the created loan as a DTO
   */
  public LoanDTO createLoan(Long userId, Long bookItemId) {
    CreateLoanDTO dto = new CreateLoanDTO();
    dto.setUserId(userId);
    dto.setBookItemId(bookItemId);
    dto.setLimitAt(LocalDateTime.now().plusDays(DEFAULT_LOAN_DURATION_DAYS));
    return createLoan(dto);
  }

  // ============================================
  // LOAN RETURN
  // ============================================

  /**
   * Processes the return of a borrowed book.
   * Sets the returnedAt timestamp and updates the book item status to AVAILABLE.
   *
   * @param loanId the ID of the loan to return
   * @return the updated loan as a DTO
   * @throws ResourceNotFoundException if loan not found
   * @throws IllegalStateException     if the loan has already been returned
   */
  public LoanDTO returnLoan(Long loanId) {
    Loan loan = findLoanOrThrow(loanId);

    if (!loan.isActive()) {
      throw new IllegalStateException("Loan with ID " + loanId + " has already been returned");
    }

    loan.setReturnedAt(LocalDateTime.now());

    // Update book item status to AVAILABLE
    BookItem bookItem = loan.getBookItem();
    bookItem.setStatus(BookStatus.AVAILABLE);
    bookItemRepository.save(bookItem);

    Loan savedLoan = loanRepository.save(loan);
    return convertToDTO(savedLoan);
  }

  /**
   * Processes the return of a book by book item barcode.
   * Finds the active loan for the book item and returns it.
   *
   * @param barcode the barcode of the book item being returned
   * @return the updated loan as a DTO
   * @throws ResourceNotFoundException if book item or active loan not found
   */
  public LoanDTO returnLoanByBarcode(String barcode) {
    BookItem bookItem = bookItemRepository.findByBarcode(barcode)
        .orElseThrow(() -> new ResourceNotFoundException("BookItem", "barcode", barcode));

    Loan loan = loanRepository.findByBookItemAndReturnedAtIsNull(bookItem)
        .orElseThrow(() -> new ResourceNotFoundException(
            "No active loan found for book item with barcode: " + barcode));

    return returnLoan(loan.getId());
  }

  // ============================================
  // LOAN RETRIEVAL
  // ============================================

  /**
   * Retrieves all loans in the system.
   *
   * @return list of all loans as DTOs
   */
  @Transactional(readOnly = true)
  public List<LoanDTO> getAllLoans() {
    return loanRepository.findAll().stream()
        .map(this::convertToDTO)
        .toList();
  }

  /**
   * Retrieves a loan by its ID.
   *
   * @param id the loan ID
   * @return the loan as a DTO
   * @throws ResourceNotFoundException if loan not found
   */
  @Transactional(readOnly = true)
  public LoanDTO getLoanById(Long id) {
    Loan loan = findLoanOrThrow(id);
    return convertToDTO(loan);
  }

  /**
   * Retrieves all loans for a specific user.
   *
   * @param userId the user ID
   * @return list of loans for the user
   * @throws ResourceNotFoundException if user not found
   */
  @Transactional(readOnly = true)
  public List<LoanDTO> getLoansByUserId(Long userId) {
    // Validate user exists
    findUserOrThrow(userId);
    return loanRepository.findByUserId(userId).stream()
        .map(this::convertToDTO)
        .toList();
  }

  /**
   * Retrieves all active (not returned) loans for a specific user.
   *
   * @param userId the user ID
   * @return list of active loans for the user
   */
  @Transactional(readOnly = true)
  public List<LoanDTO> getActiveLoansForUser(Long userId) {
    findUserOrThrow(userId);
    return loanRepository.findByUserIdAndReturnedAtIsNull(userId).stream()
        .map(this::convertToDTO)
        .toList();
  }

  /**
   * Retrieves the loan history for a specific user (returned loans).
   *
   * @param userId the user ID
   * @return list of returned loans for the user
   */
  @Transactional(readOnly = true)
  public List<LoanDTO> getLoanHistoryForUser(Long userId) {
    findUserOrThrow(userId);
    return loanRepository.findByUserIdAndReturnedAtIsNotNull(userId).stream()
        .map(this::convertToDTO)
        .toList();
  }

  /**
   * Retrieves all active loans in the system.
   *
   * @return list of all active loans
   */
  @Transactional(readOnly = true)
  public List<LoanDTO> getAllActiveLoans() {
    return loanRepository.findByReturnedAtIsNull().stream()
        .map(this::convertToDTO)
        .toList();
  }

  /**
   * Retrieves the loan history for a specific book item.
   *
   * @param bookItemId the book item ID
   * @return list of loans for the book item, ordered by most recent first
   */
  @Transactional(readOnly = true)
  public List<LoanDTO> getLoanHistoryForBookItem(Long bookItemId) {
    findBookItemOrThrow(bookItemId);
    return loanRepository.findLoanHistoryByBookItemId(bookItemId).stream()
        .map(this::convertToDTO)
        .toList();
  }

  // ============================================
  // OVERDUE LOAN OPERATIONS
  // ============================================

  /**
   * Retrieves all overdue loans in the system.
   *
   * @return list of all overdue loans
   */
  @Transactional(readOnly = true)
  public List<LoanDTO> getOverdueLoans() {
    return loanRepository.findOverdueLoans(LocalDateTime.now()).stream()
        .map(this::convertToDTO)
        .toList();
  }

  /**
   * Retrieves all overdue loans for a specific user.
   *
   * @param userId the user ID
   * @return list of overdue loans for the user
   */
  @Transactional(readOnly = true)
  public List<LoanDTO> getOverdueLoansForUser(Long userId) {
    findUserOrThrow(userId);
    return loanRepository.findOverdueLoansByUserId(userId, LocalDateTime.now()).stream()
        .map(this::convertToDTO)
        .toList();
  }

  /**
   * Checks if a user has any overdue loans.
   *
   * @param userId the user ID
   * @return true if the user has overdue loans
   */
  @Transactional(readOnly = true)
  public boolean userHasOverdueLoans(Long userId) {
    User user = findUserOrThrow(userId);
    return loanRepository.hasOverdueLoans(user, LocalDateTime.now());
  }

  /**
   * Gets the count of overdue loans in the system.
   *
   * @return the count of overdue loans
   */
  @Transactional(readOnly = true)
  public long countOverdueLoans() {
    return loanRepository.countOverdueLoans(LocalDateTime.now());
  }

  // ============================================
  // DUE SOON OPERATIONS
  // ============================================

  /**
   * Retrieves loans that are due within the specified number of days.
   * Useful for sending reminder notifications.
   *
   * @param days the number of days to look ahead
   * @return list of loans due within the specified days
   */
  @Transactional(readOnly = true)
  public List<LoanDTO> getLoansDueSoon(int days) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime endTime = now.plusDays(days);
    return loanRepository.findLoansDueBetween(now, endTime).stream()
        .map(this::convertToDTO)
        .toList();
  }

  /**
   * Retrieves loans due soon for a specific user.
   *
   * @param userId the user ID
   * @param days   the number of days to look ahead
   * @return list of loans due soon for the user
   */
  @Transactional(readOnly = true)
  public List<LoanDTO> getLoansDueSoonForUser(Long userId, int days) {
    User user = findUserOrThrow(userId);
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime endTime = now.plusDays(days);
    return loanRepository.findLoansDueBetweenForUser(user, now, endTime).stream()
        .map(this::convertToDTO)
        .toList();
  }

  // ============================================
  // LOAN EXTENSION
  // ============================================

  /**
   * Extends the due date of an active loan.
   * Adds the specified number of days to the current due date.
   *
   * @param loanId         the ID of the loan to extend
   * @param additionalDays the number of days to add
   * @return the updated loan as a DTO
   * @throws ResourceNotFoundException if loan not found
   * @throws IllegalStateException     if the loan is not active
   */
  public LoanDTO extendLoan(Long loanId, int additionalDays) {
    Loan loan = findLoanOrThrow(loanId);

    if (!loan.isActive()) {
      throw new IllegalStateException("Cannot extend a returned loan");
    }

    loan.setLimitAt(loan.getLimitAt().plusDays(additionalDays));
    Loan savedLoan = loanRepository.save(loan);
    return convertToDTO(savedLoan);
  }

  /**
   * Extends the due date of a loan by the default duration.
   *
   * @param loanId the ID of the loan to extend
   * @return the updated loan as a DTO
   */
  public LoanDTO extendLoan(Long loanId) {
    return extendLoan(loanId, DEFAULT_LOAN_DURATION_DAYS);
  }

  // ============================================
  // STATISTICS
  // ============================================

  /**
   * Gets the count of active loans for a user.
   *
   * @param userId the user ID
   * @return the count of active loans
   */
  @Transactional(readOnly = true)
  public long countActiveLoansForUser(Long userId) {
    return loanRepository.countByUserIdAndReturnedAtIsNull(userId);
  }

  /**
   * Gets the total count of loans for a user (active and returned).
   *
   * @param userId the user ID
   * @return the total count of loans
   */
  @Transactional(readOnly = true)
  public long countTotalLoansForUser(Long userId) {
    return loanRepository.countByUserId(userId);
  }

  /**
   * Checks if a book item is currently borrowed.
   *
   * @param bookItemId the book item ID
   * @return true if the book item has an active loan
   */
  @Transactional(readOnly = true)
  public boolean isBookItemBorrowed(Long bookItemId) {
    return loanRepository.existsByBookItemIdAndReturnedAtIsNull(bookItemId);
  }

  // ============================================
  // VALIDATION HELPERS
  // ============================================

  /**
   * Validates that a user is eligible to borrow books.
   * Checks for maximum active loans and overdue books.
   *
   * @param user the user to validate
   * @throws IllegalStateException if user cannot borrow
   */
  private void validateUserCanBorrow(User user) {
    // Check for maximum active loans
    long activeLoans = loanRepository.countByUserAndReturnedAtIsNull(user);
    if (activeLoans >= MAX_ACTIVE_LOANS_PER_USER) {
      throw new IllegalStateException(
          "User has reached the maximum number of active loans (" + MAX_ACTIVE_LOANS_PER_USER + ")");
    }

    // Check for overdue loans
    if (loanRepository.hasOverdueLoans(user, LocalDateTime.now())) {
      throw new IllegalStateException(
          "User has overdue loans and cannot borrow until they are returned");
    }
  }

  /**
   * Validates that a book item is available for borrowing.
   *
   * @param bookItem the book item to validate
   * @throws IllegalStateException if book item is not available
   */
  private void validateBookItemAvailable(BookItem bookItem) {
    if (bookItem.getStatus() != BookStatus.AVAILABLE) {
      throw new IllegalStateException(
          "Book item with barcode '" + bookItem.getBarcode() + "' is not available. Current status: "
              + bookItem.getStatus());
    }

    // Double-check no active loan exists
    if (loanRepository.existsByBookItemAndReturnedAtIsNull(bookItem)) {
      throw new IllegalStateException(
          "Book item with barcode '" + bookItem.getBarcode() + "' is already borrowed");
    }
  }

  // ============================================
  // ENTITY FINDERS
  // ============================================

  /**
   * Finds a user by ID or throws an exception.
   *
   * @param id the user ID
   * @return the user entity
   * @throws ResourceNotFoundException if not found
   */
  private User findUserOrThrow(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
  }

  /**
   * Finds a book item by ID or throws an exception.
   *
   * @param id the book item ID
   * @return the book item entity
   * @throws ResourceNotFoundException if not found
   */
  private BookItem findBookItemOrThrow(Long id) {
    return bookItemRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("BookItem", "id", id));
  }

  /**
   * Finds a loan by ID or throws an exception.
   *
   * @param id the loan ID
   * @return the loan entity
   * @throws ResourceNotFoundException if not found
   */
  private Loan findLoanOrThrow(Long id) {
    return loanRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", id));
  }

  // ============================================
  // DTO CONVERTER
  // ============================================

  /**
   * Converts a Loan entity to its DTO representation.
   * Includes user and book item details.
   *
   * @param loan the entity to convert
   * @return the DTO representation
   */
  private LoanDTO convertToDTO(Loan loan) {
    LoanDTO dto = new LoanDTO();
    dto.setId(loan.getId());
    dto.setUserId(loan.getUser().getId());
    dto.setUsername(loan.getUser().getUsername());
    dto.setBookItemId(loan.getBookItem().getId());
    dto.setBookItemBarcode(loan.getBookItem().getBarcode());
    dto.setBookTitle(loan.getBookItem().getBookDefinition().getTitle());
    dto.setCreatedAt(loan.getCreatedAt());
    dto.setLimitAt(loan.getLimitAt());
    dto.setReturnedAt(loan.getReturnedAt());
    dto.setActive(loan.isActive());
    dto.setOverdue(loan.isOverdue());
    return dto;
  }
}
