package com.devaldrete.controller;

import java.util.List;
import java.util.Optional;

import com.devaldrete.domain.BookDefinition;
import com.devaldrete.domain.BookItem;
import com.devaldrete.domain.BookStatus;
import com.devaldrete.dto.BookDefinitionDTO;
import com.devaldrete.dto.BookItemDTO;
import com.devaldrete.dto.CreateBookDefinitionDTO;
import com.devaldrete.dto.CreateBookItemDTO;
import com.devaldrete.exception.ResourceNotFoundException;
import com.devaldrete.repository.BookDefinitionRepository;
import com.devaldrete.service.BookService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing books (definitions and physical items).
 * Provides endpoints for CRUD operations on book definitions and book items.
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

  private final BookService bookService;
  private final BookDefinitionRepository bookDefinitionRepository;

  @Autowired
  public BookController(BookService bookService, BookDefinitionRepository bookDefinitionRepository) {
    this.bookService = bookService;
    this.bookDefinitionRepository = bookDefinitionRepository;
  }

  // ============================================
  // BOOK DEFINITION ENDPOINTS
  // ============================================

  /**
   * Get all book definitions in the catalog.
   *
   * @return list of all book definitions
   */
  @GetMapping
  public ResponseEntity<List<BookDefinitionDTO>> getAllBooks() {
    List<BookDefinitionDTO> books = bookService.getAllBookDefinitions();
    return ResponseEntity.ok(books);
  }

  /**
   * Get a book definition by its ID.
   *
   * @param id the book definition ID
   * @return the book definition
   */
  @GetMapping("/{id}")
  public ResponseEntity<BookDefinitionDTO> getBookById(@PathVariable Long id) {
    BookDefinitionDTO book = bookService.getBookDefinitionById(id);
    return ResponseEntity.ok(book);
  }

  /**
   * Get a book definition by its ISBN.
   *
   * @param isbn the ISBN to search for
   * @return the book definition if found
   */
  @GetMapping("/isbn/{isbn}")
  public ResponseEntity<BookDefinitionDTO> getBookByIsbn(@PathVariable String isbn) {
    Optional<BookDefinitionDTO> book = bookService.getBookDefinitionByIsbn(isbn);
    return ResponseEntity.of(book);
  }

  /**
   * Create a new book definition in the catalog.
   *
   * @param createDTO the book definition details
   * @return the created book definition
   */
  @PostMapping
  public ResponseEntity<BookDefinitionDTO> createBookDefinition(
      @Valid @RequestBody CreateBookDefinitionDTO createDTO) {
    BookDefinition bookDefinition = new BookDefinition();
    bookDefinition.setIsbn(createDTO.getIsbn());
    bookDefinition.setTitle(createDTO.getTitle());
    bookDefinition.setAuthor(createDTO.getAuthor());
    bookDefinition.setPublisher(createDTO.getPublisher());

    BookDefinitionDTO created = bookService.addBookDefinition(bookDefinition);
    return new ResponseEntity<>(created, HttpStatus.CREATED);
  }

  /**
   * Update an existing book definition.
   *
   * @param id the book definition ID
   * @param createDTO the updated book definition details
   * @return the updated book definition
   */
  @PutMapping("/{id}")
  public ResponseEntity<BookDefinitionDTO> updateBookDefinition(
      @PathVariable Long id,
      @Valid @RequestBody CreateBookDefinitionDTO createDTO) {
    BookDefinition bookDefinition = new BookDefinition();
    bookDefinition.setIsbn(createDTO.getIsbn());
    bookDefinition.setTitle(createDTO.getTitle());
    bookDefinition.setAuthor(createDTO.getAuthor());
    bookDefinition.setPublisher(createDTO.getPublisher());

    BookDefinitionDTO updated = bookService.updateBookDefinition(id, bookDefinition);
    return ResponseEntity.ok(updated);
  }

  /**
   * Delete a book definition from the catalog.
   * Note: This will fail if there are book items associated with this definition.
   *
   * @param id the book definition ID
   * @return no content on success
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteBookDefinition(@PathVariable Long id) {
    bookService.deleteBookDefinition(id);
    return ResponseEntity.noContent().build();
  }

  // ============================================
  // BOOK ITEM ENDPOINTS
  // ============================================

  /**
   * Get all book items (physical copies) in the library.
   *
   * @return list of all book items
   */
  @GetMapping("/items")
  public ResponseEntity<List<BookItemDTO>> getAllBookItems() {
    List<BookItemDTO> bookItems = bookService.getAllBookItems();
    return ResponseEntity.ok(bookItems);
  }

  /**
   * Get all book items for a specific book definition.
   *
   * @param id the book definition ID
   * @return list of book items for the given definition
   */
  @GetMapping("/{id}/items")
  public ResponseEntity<List<BookItemDTO>> getBookItemsByBookDefinitionId(@PathVariable Long id) {
    List<BookItemDTO> bookItems = bookService.getBookItemsByBookDefinitionId(id);
    return ResponseEntity.ok(bookItems);
  }

  /**
   * Get all available book items for a specific book definition.
   * Useful for checking if a book can be borrowed.
   *
   * @param id the book definition ID
   * @return list of available book items
   */
  @GetMapping("/{id}/items/available")
  public ResponseEntity<List<BookItemDTO>> getAvailableBookItemsByBookDefinitionId(@PathVariable Long id) {
    List<BookItemDTO> bookItems = bookService.getAvailableBookItemsByBookDefinitionId(id);
    return ResponseEntity.ok(bookItems);
  }

  /**
   * Get a book item by its barcode.
   * This is the primary way to look up a physical book when it's returned.
   *
   * @param barcode the book item's barcode
   * @return the book item
   */
  @GetMapping("/items/barcode/{barcode}")
  public ResponseEntity<BookItemDTO> getBookItemByBarcode(@PathVariable String barcode) {
    BookItemDTO bookItem = bookService.getBookItemByBarcode(barcode);
    return ResponseEntity.ok(bookItem);
  }

  /**
   * Get a book item by its ID.
   *
   * @param itemId the book item ID
   * @return the book item
   */
  @GetMapping("/items/{itemId}")
  public ResponseEntity<BookItemDTO> getBookItemById(@PathVariable Long itemId) {
    BookItemDTO bookItem = bookService.getBookItemById(itemId);
    return ResponseEntity.ok(bookItem);
  }

  /**
   * Create a new book item (physical copy) for a book definition.
   *
   * @param createDTO the book item details including barcode and book definition ID
   * @return the created book item
   */
  @PostMapping("/items")
  public ResponseEntity<BookItemDTO> createBookItem(@Valid @RequestBody CreateBookItemDTO createDTO) {
    // Validate book definition exists
    BookDefinition bookDefinition = bookDefinitionRepository.findById(createDTO.getBookDefinitionId())
        .orElseThrow(() -> new ResourceNotFoundException("BookDefinition", "id", createDTO.getBookDefinitionId()));

    BookItem bookItem = new BookItem();
    bookItem.setBarcode(createDTO.getBarcode());
    bookItem.setBookDefinition(bookDefinition);
    bookItem.setStatus(BookStatus.AVAILABLE);

    BookItemDTO created = bookService.addBookItem(bookItem);
    return new ResponseEntity<>(created, HttpStatus.CREATED);
  }

  /**
   * Update the status of a book item by its barcode.
   * Use this endpoint to manually mark a book as AVAILABLE, BORROWED, or LOST.
   *
   * @param barcode the book item's barcode
   * @param status the new status
   * @return the updated book item
   */
  @PatchMapping("/items/barcode/{barcode}/status")
  public ResponseEntity<BookItemDTO> updateBookItemStatus(
      @PathVariable String barcode,
      @RequestParam BookStatus status) {
    BookItemDTO updated = bookService.updateBookItemStatus(barcode, status);
    return ResponseEntity.ok(updated);
  }

  /**
   * Soft delete a book item by its barcode.
   * The book must not be currently borrowed.
   *
   * @param barcode the book item's barcode
   * @return no content on success
   */
  @DeleteMapping("/items/barcode/{barcode}")
  public ResponseEntity<Void> softDeleteBookItemByBarcode(@PathVariable String barcode) {
    bookService.softDeleteBookItemByBarcode(barcode);
    return ResponseEntity.noContent().build();
  }

  /**
   * Soft delete a book item by its ID.
   * The book must not be currently borrowed.
   *
   * @param itemId the book item ID
   * @return no content on success
   */
  @DeleteMapping("/items/{itemId}")
  public ResponseEntity<Void> softDeleteBookItemById(@PathVariable Long itemId) {
    bookService.softDeleteBookItemById(itemId);
    return ResponseEntity.noContent().build();
  }

  /**
   * Restore a soft-deleted book item by its barcode.
   *
   * @param barcode the book item's barcode
   * @return the restored book item
   */
  @PostMapping("/items/barcode/{barcode}/restore")
  public ResponseEntity<BookItemDTO> restoreBookItem(@PathVariable String barcode) {
    BookItemDTO restored = bookService.restoreBookItemByBarcode(barcode);
    return ResponseEntity.ok(restored);
  }

  /**
   * Get all soft-deleted book items.
   * Admin functionality for viewing and potentially restoring deleted items.
   *
   * @return list of all soft-deleted book items
   */
  @GetMapping("/items/deleted")
  public ResponseEntity<List<BookItemDTO>> getAllDeletedBookItems() {
    List<BookItemDTO> deletedItems = bookService.getAllDeletedBookItems();
    return ResponseEntity.ok(deletedItems);
  }

  /**
   * Get the count of book items for a specific book definition.
   *
   * @param id the book definition ID
   * @return the total count of book items
   */
  @GetMapping("/{id}/items/count")
  public ResponseEntity<Long> countBookItemsByBookDefinitionId(@PathVariable Long id) {
    long count = bookService.countBookItemsByBookDefinitionId(id);
    return ResponseEntity.ok(count);
  }

  /**
   * Get the count of available book items for a specific book definition.
   *
   * @param id the book definition ID
   * @return the count of available book items
   */
  @GetMapping("/{id}/items/available/count")
  public ResponseEntity<Long> countAvailableBookItemsByBookDefinitionId(@PathVariable Long id) {
    long count = bookService.countAvailableBookItemsByBookDefinitionId(id);
    return ResponseEntity.ok(count);
  }

}
