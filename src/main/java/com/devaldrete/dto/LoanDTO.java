package com.devaldrete.dto;

import java.time.LocalDateTime;

import com.devaldrete.domain.BookItem;
import com.devaldrete.domain.User;

import jakarta.validation.constraints.NotBlank;

public class LoanDTO {
  private Long id;

  private LocalDateTime createdAt;

  private LocalDateTime limitAt;
}
