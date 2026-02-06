package com.devaldrete.repository;

import java.util.List;
import java.util.Optional;

import com.devaldrete.domain.BookDefinition;
import com.devaldrete.domain.BookItem;
import com.devaldrete.domain.BookStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing BookItem entities.
 * Note: By default, all queries exclude soft-deleted items due to @SQLRestriction on BookItem.
 * Use methods with "IncludingDeleted" suffix to include soft-deleted items.
 */
@Repository
public interface BookItemRepository extends JpaRepository<BookItem, Long> {

  /**
   * Finds a book item by its unique barcode.
   * Excludes soft-deleted items.
   *
   * @param barcode the unique barcode of the book item
   * @return an Optional containing the book item if found
   */
  Optional<BookItem> findByBarcode(String barcode);

  /**
   * Finds a book item by barcode, including soft-deleted items.
   *
   * @param barcode the unique barcode of the book item
   * @return an Optional containing the book item if found
   */
  @Query("SELECT b FROM BookItem b WHERE b.barcode = :barcode")
  Optional<BookItem> findByBarcodeIncludingDeleted(@Param("barcode") String barcode);

  /**
   * Finds all book items belonging to a specific book definition.
   * Excludes soft-deleted items.
   *
   * @param bookDefinition the book definition to search for
   * @return list of book items for the given definition
   */
  List<BookItem> findByBookDefinition(BookDefinition bookDefinition);

  /**
   * Counts all book items for a specific book definition.
   * Excludes soft-deleted items.
   *
   * @param bookDefinition the book definition to count items for
   * @return the count of book items
   */
  long countByBookDefinition(BookDefinition bookDefinition);

  /**
   * Counts available book items for a specific book definition.
   * Excludes soft-deleted items.
   *
   * @param bookDefinition the book definition to count items for
   * @return the count of available book items
   */
  long countByBookDefinitionAndStatus(BookDefinition bookDefinition, BookStatus status);

  /**
   * Physically deletes a book item by barcode.
   * Consider using soft delete via the service layer instead.
   *
   * @param barcode the barcode of the item to delete
   */
  @Modifying
  void deleteByBarcode(String barcode);

  /**
   * Finds all book items for a definition with a specific status.
   * Excludes soft-deleted items.
   *
   * @param bookDefinition the book definition to search for
   * @param status the status to filter by
   * @return list of book items matching the criteria
   */
  List<BookItem> findByBookDefinitionAndStatus(BookDefinition bookDefinition, BookStatus status);

  /**
   * Finds all book items with a specific status.
   * Excludes soft-deleted items.
   *
   * @param status the status to filter by
   * @return list of book items with the given status
   */
  List<BookItem> findByStatus(BookStatus status);

  /**
   * Finds all soft-deleted book items.
   *
   * @return list of soft-deleted book items
   */
  @Query("SELECT b FROM BookItem b WHERE b.deleted = true")
  List<BookItem> findAllDeleted();

  /**
   * Checks if a book item with the given barcode exists (excluding deleted).
   *
   * @param barcode the barcode to check
   * @return true if a non-deleted item exists with this barcode
   */
  boolean existsByBarcode(String barcode);

  /**
   * Checks if a book item with the given barcode exists (including deleted).
   *
   * @param barcode the barcode to check
   * @return true if any item (deleted or not) exists with this barcode
   */
  @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM BookItem b WHERE b.barcode = :barcode")
  boolean existsByBarcodeIncludingDeleted(@Param("barcode") String barcode);
}
