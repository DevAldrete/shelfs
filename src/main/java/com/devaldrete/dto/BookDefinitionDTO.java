package com.devaldrete.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDefinitionDTO {
  private Long id;
  private String isbn;
  private String title;
  private String author;
  private String publisher;
}
