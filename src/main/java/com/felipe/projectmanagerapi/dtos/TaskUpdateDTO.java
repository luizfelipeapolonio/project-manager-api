package com.felipe.projectmanagerapi.dtos;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

public record TaskUpdateDTO(

  @Nullable
  @Length(min = 1, max = 70, message = "O nome deve ter entre 1 e 70 caracteres")
  String name,

  @Nullable
  String description,

  @Nullable
  @Pattern(regexp = "^\\d+\\.\\d{2}$", message = "Custo inválido! Digite no formato válido. Ex: 1200.00")
  String cost
) {}
