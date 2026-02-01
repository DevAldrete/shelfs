package com.devaldrete.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLoanDTO {
  @NotNull(message = "User ID is required")
  private Long userId;

  @NotNull(message = "Book item ID is required")
  private Long bookItemId;

  @NotNull(message = "Limit date is required")
  private LocalDateTime limitAt;
}
