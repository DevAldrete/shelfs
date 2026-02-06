package com.devaldrete.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.devaldrete.domain.BookDefinition;
import com.devaldrete.domain.BookItem;
import com.devaldrete.domain.BookStatus;
import com.devaldrete.dto.BookDefinitionDTO;
import com.devaldrete.dto.BookItemDTO;
import com.devaldrete.exception.BookItemInLoanException;
import com.devaldrete.exception.ResourceNotFoundException;
import com.devaldrete.repository.BookDefinitionRepository;
import com.devaldrete.repository.BookItemRepository;
import com.devaldrete.repository.LoanRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for managing book definitions and book items.
 * Provides business logic for CRUD operations on books, including
 * soft delete functionality and loan validation before deletion.
 */
@Service
@Transactional
public class BookService {

  private final BookDefinitionRepository bookDefinitionRepository;
  private final BookItemRepository bookItemRepository;
  private final LoanRepository loanRepository;

  @Autowired
  public BookService(
      BookDefinitionRepository bookDefinitionRepository,
      BookItemRepository bookItemRepository,
      LoanRepository loanRepository) {
    this.bookDefinitionRepository = bookDefinitionRepository;
    this.bookItemRepository = bookItemRepository;
    this.loanRepository = loanRepository;
  }

  // ============================================
  // BOOK DEFINITION OPERATIONS
  // ============================================

  /**
   * Retrieves all book definitions from the database.
   *
   * @return list of all book definitions as DTOs
   */
  @Transactional(readOnly = true)
  public List<BookDefinitionDTO> getAllBookDefinitions() {
    return bookDefinitionRepository.findAll().stream()
        .map(this::convertBookDefinitionToDTO)
        .toList();
  }

  /**
   * Retrieves a book definition by its unique identifier.
   *
   * @param id the book definition ID
   * @return the book definition as a DTO
   * @throws ResourceNotFoundException if no book definition exists with the given ID
   */
  @Transactional(readOnly = true)
  public BookDefinitionDTO getBookDefinitionById(Long id) {
    BookDefinition bookDefinition = findBookDefinitionOrThrow(id);
    return convertBookDefinitionToDTO(bookDefinition);
  }

  /**
   * Retrieves a book definition by its ISBN.
   *
   * @param isbn the ISBN to search for
   * @return an Optional containing the book definition if found
   */
  @Transactional(readOnly = true)
  public Optional<BookDefinitionDTO> getBookDefinitionByIsbn(String isbn) {
    return bookDefinitionRepository.findByIsbn(isbn)
        .map(this::convertBookDefinitionToDTO);
  }

  /**
   * Creates a new book definition in the database.
   *
   * @param bookDefinition the book definition entity to save
   * @return the saved book definition as a DTO
   */
  public BookDefinitionDTO addBookDefinition(BookDefinition bookDefinition) {
    BookDefinition saved = bookDefinitionRepository.save(bookDefinition);
    return convertBookDefinitionToDTO(saved);
  }

  /**
   * Updates an existing book definition with new values.
   * Only updates title, author, ISBN, and publisher fields.
   *
   * @param id the ID of the book definition to update
   * @param updatedDefinition the entity containing updated values
   * @return the updated book definition as a DTO
   * @throws ResourceNotFoundException if no book definition exists with the given ID
   */
  public BookDefinitionDTO updateBookDefinition(Long id, BookDefinition updatedDefinition) {
    BookDefinition bookDefinition = findBookDefinitionOrThrow(id);

    bookDefinition.setTitle(updatedDefinition.getTitle());
    bookDefinition.setAuthor(updatedDefinition.getAuthor());
    bookDefinition.setIsbn(updatedDefinition.getIsbn());
    bookDefinition.setPublisher(updatedDefinition.getPublisher());

    BookDefinition saved = bookDefinitionRepository.save(bookDefinition);
    return convertBookDefinitionToDTO(saved);
  }

  /**
   * Deletes a book definition by its ID.
   * Note: This will fail if there are book items associated with this definition.
   *
   * @param id the ID of the book definition to delete
   * @throws ResourceNotFoundException if no book definition exists with the given ID
   */
  public void deleteBookDefinition(Long id) {
    BookDefinition bookDefinition = findBookDefinitionOrThrow(id);
    bookDefinitionRepository.delete(bookDefinition);
  }

  // ============================================
  // BOOK ITEM OPERATIONS
  // ============================================

  /**
   * Retrieves all book items from the database.
   * Soft-deleted items are excluded automatically.
   *
   * @return list of all active book items as DTOs
   */
  @Transactional(readOnly = true)
  public List<BookItemDTO> getAllBookItems() {
    return bookItemRepository.findAll().stream()
        .map(this::convertBookItemToDTO)
        .toList();
  }

  /**
   * Retrieves a book item by its unique barcode.
   *
   * @param barcode the barcode to search for
   * @return the book item as a DTO
   * @throws ResourceNotFoundException if no book item exists with the given barcode
   */
  @Transactional(readOnly = true)
  public BookItemDTO getBookItemByBarcode(String barcode) {
    BookItem bookItem = findBookItemByBarcodeOrThrow(barcode);
    return convertBookItemToDTO(bookItem);
  }

  /**
   * Retrieves a book item by its ID.
   *
   * @param id the book item ID
   * @return the book item as a DTO
   * @throws ResourceNotFoundException if no book item exists with the given ID
   */
  @Transactional(readOnly = true)
  public BookItemDTO getBookItemById(Long id) {
    BookItem bookItem = findBookItemByIdOrThrow(id);
    return convertBookItemToDTO(bookItem);
  }

  /**
   * Retrieves all book items for a specific book definition.
   *
   * @param bookDefinitionId the book definition ID
   * @return list of book items for the given definition
   * @throws ResourceNotFoundException if no book definition exists with the given ID
   */
  @Transactional(readOnly = true)
  public List<BookItemDTO> getBookItemsByBookDefinitionId(Long bookDefinitionId) {
    BookDefinition bookDefinition = findBookDefinitionOrThrow(bookDefinitionId);
    return bookItemRepository.findByBookDefinition(bookDefinition).stream()
        .map(this::convertBookItemToDTO)
        .toList();
  }

  /**
   * Retrieves all available book items for a specific book definition.
   * Only returns items with status AVAILABLE.
   *
   * @param bookDefinitionId the book definition ID
   * @return list of available book items for the given definition
   * @throws ResourceNotFoundException if no book definition exists with the given ID
   */
  @Transactional(readOnly = true)
  public List<BookItemDTO> getAvailableBookItemsByBookDefinitionId(Long bookDefinitionId) {
    BookDefinition bookDefinition = findBookDefinitionOrThrow(bookDefinitionId);
    return bookItemRepository.findByBookDefinitionAndStatus(bookDefinition, BookStatus.AVAILABLE).stream()
        .map(this::convertBookItemToDTO)
        .toList();
  }

  /**
   * Counts the total number of book items for a specific book definition.
   *
   * @param bookDefinitionId the book definition ID
   * @return the count of book items
   * @throws ResourceNotFoundException if no book definition exists with the given ID
   */
  @Transactional(readOnly = true)
  public long countBookItemsByBookDefinitionId(Long bookDefinitionId) {
    BookDefinition bookDefinition = findBookDefinitionOrThrow(bookDefinitionId);
    return bookItemRepository.countByBookDefinition(bookDefinition);
  }

  /**
   * Counts available book items for a specific book definition.
   *
   * @param bookDefinitionId the book definition ID
   * @return the count of available book items
   * @throws ResourceNotFoundException if no book definition exists with the given ID
   */
  @Transactional(readOnly = true)
  public long countAvailableBookItemsByBookDefinitionId(Long bookDefinitionId) {
    BookDefinition bookDefinition = findBookDefinitionOrThrow(bookDefinitionId);
    return bookItemRepository.countByBookDefinitionAndStatus(bookDefinition, BookStatus.AVAILABLE);
  }

  /**
   * Creates a new book item in the database.
   * Sets the acquisition date to current time if not provided.
   *
   * @param bookItem the book item entity to save
   * @return the saved book item as a DTO
   */
  public BookItemDTO addBookItem(BookItem bookItem) {
    if (bookItem.getAcquisitionDate() == null) {
      bookItem.setAcquisitionDate(LocalDateTime.now());
    }
    if (bookItem.getStatus() == null) {
      bookItem.setStatus(BookStatus.AVAILABLE);
    }
    BookItem saved = bookItemRepository.save(bookItem);
    return convertBookItemToDTO(saved);
  }

  /**
   * Updates the status of a book item.
   *
   * @param barcode the barcode of the book item to update
   * @param status the new status to set
   * @return the updated book item as a DTO
   * @throws ResourceNotFoundException if no book item exists with the given barcode
   */
  public BookItemDTO updateBookItemStatus(String barcode, BookStatus status) {
    BookItem bookItem = findBookItemByBarcodeOrThrow(barcode);
    bookItem.setStatus(status);
    BookItem saved = bookItemRepository.save(bookItem);
    return convertBookItemToDTO(saved);
  }

  /**
   * Soft deletes a book item by its barcode.
   * This method validates that the book item is not currently borrowed before deletion.
   * The item is marked as deleted rather than being physically removed from the database.
   *
   * @param barcode the barcode of the book item to delete
   * @throws ResourceNotFoundException if no book item exists with the given barcode
   * @throws BookItemInLoanException if the book item is currently borrowed (has an active loan)
   */
  public void softDeleteBookItemByBarcode(String barcode) {
    BookItem bookItem = findBookItemByBarcodeOrThrow(barcode);
    validateBookItemNotInActiveLoan(bookItem);
    bookItem.softDelete();
    bookItemRepository.save(bookItem);
  }

  /**
   * Soft deletes a book item by its ID.
   * This method validates that the book item is not currently borrowed before deletion.
   * The item is marked as deleted rather than being physically removed from the database.
   *
   * @param id the ID of the book item to delete
   * @throws ResourceNotFoundException if no book item exists with the given ID
   * @throws BookItemInLoanException if the book item is currently borrowed (has an active loan)
   */
  public void softDeleteBookItemById(Long id) {
    BookItem bookItem = findBookItemByIdOrThrow(id);
    validateBookItemNotInActiveLoan(bookItem);
    bookItem.softDelete();
    bookItemRepository.save(bookItem);
  }

  /**
   * Restores a soft-deleted book item by its barcode.
   * The item will be visible again in normal queries after restoration.
   *
   * @param barcode the barcode of the book item to restore
   * @throws ResourceNotFoundException if no deleted book item exists with the given barcode
   */
  public BookItemDTO restoreBookItemByBarcode(String barcode) {
    BookItem bookItem = bookItemRepository.findByBarcodeIncludingDeleted(barcode)
        .orElseThrow(() -> new ResourceNotFoundException("BookItem", "barcode", barcode));

    if (!bookItem.isDeleted()) {
      throw new IllegalStateException("Book item with barcode '" + barcode + "' is not deleted");
    }

    bookItem.restore();
    BookItem saved = bookItemRepository.save(bookItem);
    return convertBookItemToDTO(saved);
  }

  /**
   * Retrieves all soft-deleted book items.
   * Useful for admin functionality to view and potentially restore deleted items.
   *
   * @return list of all soft-deleted book items as DTOs
   */
  @Transactional(readOnly = true)
  public List<BookItemDTO> getAllDeletedBookItems() {
    return bookItemRepository.findAllDeleted().stream()
        .map(this::convertBookItemToDTO)
        .toList();
  }

  /**
   * Permanently deletes a book item from the database.
   * Use with caution - this operation cannot be undone.
   * Validates that the item is not in an active loan.
   *
   * @param barcode the barcode of the book item to permanently delete
   * @throws ResourceNotFoundException if no book item exists with the given barcode
   * @throws BookItemInLoanException if the book item is currently borrowed
   * @deprecated Prefer using {@link #softDeleteBookItemByBarcode(String)} instead
   */
  @Deprecated
  public void hardDeleteBookItemByBarcode(String barcode) {
    BookItem bookItem = bookItemRepository.findByBarcodeIncludingDeleted(barcode)
        .orElseThrow(() -> new ResourceNotFoundException("BookItem", "barcode", barcode));
    validateBookItemNotInActiveLoan(bookItem);
    bookItemRepository.delete(bookItem);
  }

  // ============================================
  // VALIDATION HELPERS
  // ============================================

  /**
   * Validates that a book item is not currently in an active loan.
   * An active loan is one where the returnedAt date is null.
   *
   * @param bookItem the book item to validate
   * @throws BookItemInLoanException if the book item has an active loan
   */
  private void validateBookItemNotInActiveLoan(BookItem bookItem) {
    boolean hasActiveLoan = loanRepository.existsByBookItemAndReturnedAtIsNull(bookItem);
    if (hasActiveLoan) {
      throw new BookItemInLoanException(bookItem.getBarcode());
    }
  }

  // ============================================
  // ENTITY FINDERS
  // ============================================

  /**
   * Finds a book definition by ID or throws an exception.
   *
   * @param id the book definition ID
   * @return the book definition entity
   * @throws ResourceNotFoundException if not found
   */
  private BookDefinition findBookDefinitionOrThrow(Long id) {
    return bookDefinitionRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("BookDefinition", "id", id));
  }

  /**
   * Finds a book item by barcode or throws an exception.
   *
   * @param barcode the book item barcode
   * @return the book item entity
   * @throws ResourceNotFoundException if not found
   */
  private BookItem findBookItemByBarcodeOrThrow(String barcode) {
    return bookItemRepository.findByBarcode(barcode)
        .orElseThrow(() -> new ResourceNotFoundException("BookItem", "barcode", barcode));
  }

  /**
   * Finds a book item by ID or throws an exception.
   *
   * @param id the book item ID
   * @return the book item entity
   * @throws ResourceNotFoundException if not found
   */
  private BookItem findBookItemByIdOrThrow(Long id) {
    return bookItemRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("BookItem", "id", id));
  }

  // ============================================
  // DTO CONVERTERS
  // ============================================

  /**
   * Converts a BookDefinition entity to its DTO representation.
   *
   * @param bookDefinition the entity to convert
   * @return the DTO representation
   */
  private BookDefinitionDTO convertBookDefinitionToDTO(BookDefinition bookDefinition) {
    return new BookDefinitionDTO(
        bookDefinition.getId(),
        bookDefinition.getTitle(),
        bookDefinition.getAuthor(),
        bookDefinition.getIsbn(),
        bookDefinition.getPublisher());
  }

  /**
   * Converts a BookItem entity to its DTO representation.
   * Includes the nested BookDefinitionDTO.
   *
   * @param bookItem the entity to convert
   * @return the DTO representation
   */
  private BookItemDTO convertBookItemToDTO(BookItem bookItem) {
    return new BookItemDTO(
        bookItem.getId(),
        bookItem.getBarcode(),
        convertBookDefinitionToDTO(bookItem.getBookDefinition()),
        bookItem.getStatus(),
        bookItem.getAcquisitionDate());
  }
}
