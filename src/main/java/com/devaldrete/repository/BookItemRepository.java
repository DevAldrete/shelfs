package com.devaldrete.repository;

import com.devaldrete.domain.BookDefinition;
import com.devaldrete.domain.BookItem;
import com.devaldrete.domain.BookStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface BookItemRepository extends JpaRepository<BookItem, Long> {
  // Find by barcode
  Optional<BookItem> findByBarcode(String barcode);

  // Find by book definition
  List<BookItem> findByBookDefinition(BookDefinition bookDefinition);

  // Count by book definition
  long countByBookDefinition(BookDefinition bookDefinition);

  // Delete by barcode
  void deleteByBarcode(String barcode);

  // Find all available book items by book definition
  List<BookItem> findByBookDefinitionAndStatus(BookDefinition bookDefinition, BookStatus status);
}
