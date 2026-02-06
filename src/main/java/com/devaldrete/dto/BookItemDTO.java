package com.devaldrete.dto;

import java.time.LocalDateTime;

import com.devaldrete.domain.BookStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookItemDTO {
  private Long id;
  private String barcode;
  private BookDefinitionDTO bookDefinition;
  private BookStatus status;
  private LocalDateTime acquisitionDate;
}
