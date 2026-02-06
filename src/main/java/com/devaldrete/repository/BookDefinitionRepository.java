package com.devaldrete.repository;

import com.devaldrete.domain.BookDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookDefinitionRepository extends JpaRepository<BookDefinition, Long> {
  // Finding by title and author
  List<BookDefinition> findByTitleAndAuthor(String title, String author);

  // Finding by ISBN
  Optional<BookDefinition> findByIsbn(String isbn);
}
