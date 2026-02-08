package com.devaldrete.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new book definition.
 * Used when adding a new book title to the library catalog.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookDefinitionDTO {

  @NotBlank(message = "ISBN is required")
  @Size(max = 20, message = "ISBN must be at most 20 characters")
  private String isbn;

  @NotBlank(message = "Title is required")
  @Size(max = 255, message = "Title must be at most 255 characters")
  private String title;

  @Size(max = 100, message = "Author must be at most 100 characters")
  private String author;

  @Size(max = 100, message = "Publisher must be at most 100 characters")
  private String publisher;
}
