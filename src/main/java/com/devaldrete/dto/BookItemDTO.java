package com.devaldrete.dto;

import java.time.LocalDateTime;

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
  private String status;
  private LocalDateTime acquisitionDate;
}
