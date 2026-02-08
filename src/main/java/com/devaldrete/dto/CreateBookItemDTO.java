package com.devaldrete.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new book item (physical copy).
 * Used when adding a new physical copy of a book to the library.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookItemDTO {

  @NotBlank(message = "Barcode is required")
  @Size(max = 50, message = "Barcode must be at most 50 characters")
  private String barcode;

  @NotNull(message = "Book definition ID is required")
  private Long bookDefinitionId;
}
