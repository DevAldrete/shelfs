package com.devaldrete.service;

import com.devaldrete.domain.BookItem;
import com.devaldrete.dto.BookItemDTO;
import com.devaldrete.exception.ResourceNotFoundException;
import com.devaldrete.repository.BookDefinitionRepository;
import com.devaldrete.repository.BookItemRepository;

import jakarta.transaction.Transactional;

import com.devaldrete.domain.BookDefinition;
import com.devaldrete.dto.BookDefinitionDTO;

import com.devaldrete.domain.BookStatus;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class BookService {
  private final BookDefinitionRepository bookDefinitionRepository;
  private final BookItemRepository bookItemRepository;

  @Autowired
  public BookService(BookDefinitionRepository bookDefinitionRepository, BookItemRepository bookItemRepository) {
    this.bookDefinitionRepository = bookDefinitionRepository;
    this.bookItemRepository = bookItemRepository;
  }

  public List<BookDefinitionDTO> getAllBookDefinitions() {
    return bookDefinitionRepository.findAll().stream()
        .map(this::convertBookDefinitionToDTO)
        .toList();
  }

  public BookDefinitionDTO getBookDefinitionById(Long id) {
    BookDefinition bookDefinition = bookDefinitionRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("BookDefinition", "id", id));
    return convertBookDefinitionToDTO(bookDefinition);
  }

  public BookItemDTO getBookItemByBarcode(String barcode) {
    BookItem bookItem = bookItemRepository.findByBarcode(barcode)
        .orElseThrow(() -> new ResourceNotFoundException("BookItem", "barcode", barcode));
    return convertBookItemToDTO(bookItem);
  }

  public List<BookItemDTO> getBookItemsByBookDefinitionId(Long bookDefinitionId) {
    BookDefinition bookDefinition = bookDefinitionRepository.findById(bookDefinitionId)
        .orElseThrow(() -> new ResourceNotFoundException("BookDefinition", "id", bookDefinitionId));
    return bookItemRepository.findByBookDefinition(bookDefinition).stream()
        .map(this::convertBookItemToDTO)
        .toList();
  }

  public long countBookItemsByBookDefinitionId(Long bookDefinitionId) {
    BookDefinition bookDefinition = bookDefinitionRepository.findById(bookDefinitionId)
        .orElseThrow(() -> new ResourceNotFoundException("BookDefinition", "id", bookDefinitionId));
    return bookItemRepository.countByBookDefinition(bookDefinition);
  }

  public void deleteBookItemByBarcode(String barcode) {
    if (!bookItemRepository.findByBarcode(barcode).isPresent()) {
      throw new ResourceNotFoundException("BookItem", "barcode", barcode);
    }
    bookItemRepository.deleteByBarcode(barcode);
  }

  public List<BookItemDTO> getAvailableBookItemsByBookDefinitionId(Long bookDefinitionId) {
    BookDefinition bookDefinition = bookDefinitionRepository.findById(bookDefinitionId)
        .orElseThrow(() -> new ResourceNotFoundException("BookDefinition", "id", bookDefinitionId));
    return bookItemRepository.findByBookDefinitionAndStatus(bookDefinition, com.devaldrete.domain.BookStatus.AVAILABLE)
        .stream()
        .map(this::convertBookItemToDTO)
        .toList();
  }

  public List<BookItemDTO> getAllBookItems() {
    return bookItemRepository.findAll().stream()
        .map(this::convertBookItemToDTO)
        .toList();
  }

  public void addBookDefinition(BookDefinition bookDefinition) {
    bookDefinitionRepository.save(bookDefinition);
  }

  public void addBookItem(BookItem bookItem) {
    bookItemRepository.save(bookItem);
  }

  public void updateBookItemStatus(String barcode, BookStatus status) {
    BookItem bookItem = bookItemRepository.findByBarcode(barcode)
        .orElseThrow(() -> new ResourceNotFoundException("BookItem", "barcode", barcode));
    bookItem.setStatus(status);
    bookItemRepository.save(bookItem);
  }

  public void updateBookDefinition(Long id, BookDefinition updatedDefinition) {
    BookDefinition bookDefinition = bookDefinitionRepository.findById(id).orElseThrow(
        () -> new ResourceNotFoundException("BookDefinition", "id", id));
    bookDefinition.setTitle(updatedDefinition.getTitle());
    bookDefinition.setAuthor(updatedDefinition.getAuthor());
    bookDefinition.setIsbn(updatedDefinition.getIsbn());
    bookDefinition.setPublisher(updatedDefinition.getPublisher());
    bookDefinitionRepository.save(bookDefinition);
  }

  private BookDefinitionDTO convertBookDefinitionToDTO(BookDefinition bookDefinition) {
    return new BookDefinitionDTO(
        bookDefinition.getId(),
        bookDefinition.getTitle(),
        bookDefinition.getAuthor(),
        bookDefinition.getIsbn(),
        bookDefinition.getPublisher());
  }

  private BookItemDTO convertBookItemToDTO(BookItem bookItem) {
    return new BookItemDTO(
        bookItem.getId(),
        bookItem.getBarcode(),
        convertBookDefinitionToDTO(bookItem.getBookDefinition()),
        bookItem.getStatus(),
        bookItem.getAcquisitionDate());
  }
}
