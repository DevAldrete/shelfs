package com.devaldrete.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a physical copy of a book in the library.
 * Each BookItem has a unique barcode and references a BookDefinition.
 * Supports soft delete - items are marked as deleted rather than physically removed.
 */
@Entity
@Table(name = "book_items")
@SQLRestriction("deleted = false")
@Setter
@Getter
@NoArgsConstructor
public class BookItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String barcode;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "book_definition_id")
  private BookDefinition bookDefinition;

  @Enumerated(EnumType.STRING)
  private BookStatus status;

  @CreatedDate
  private LocalDateTime acquisitionDate;

  /**
   * Soft delete flag. When true, the item is considered deleted
   * and will be excluded from normal queries.
   */
  @Column(nullable = false)
  private boolean deleted = false;

  /**
   * Timestamp when the item was soft deleted.
   * Null if the item has not been deleted.
   */
  @Column(nullable = true)
  private LocalDateTime deletedAt;

  /**
   * Marks this book item as soft deleted.
   * Sets the deleted flag to true and records the deletion timestamp.
   */
  public void softDelete() {
    this.deleted = true;
    this.deletedAt = LocalDateTime.now();
  }

  /**
   * Restores a soft deleted book item.
   * Clears the deleted flag and deletion timestamp.
   */
  public void restore() {
    this.deleted = false;
    this.deletedAt = null;
  }

  /**
   * Checks if this book item is currently available for borrowing.
   *
   * @return true if the item status is AVAILABLE and not deleted
   */
  public boolean isAvailable() {
    return !deleted && status == BookStatus.AVAILABLE;
  }
}
