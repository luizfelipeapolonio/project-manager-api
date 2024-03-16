package com.felipe.projectmanagerapi.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

public record TaskCreateDTO(
  @NotNull(message = "O nome não deve ser nulo")
  @NotBlank(message = "O nome não deve estar em branco")
  @Length(min = 1, max = 70, message = "O nome deve ter entre 1 e 70 caracteres")
  String name,

  @NotNull(message = "A descrição não deve ser nula")
  @NotBlank(message = "A descrição não deve estar em branco")
  String description,

  @NotNull(message = "O custo não deve ser nulo")
  @NotBlank(message = "O custo não deve estar em branco")
  @Pattern(regexp = "^\\d+\\.\\d{2}$", message = "Custo inválido! Digite no formato válido. Ex: 1200.00")
  String cost,

  @NotNull(message = "O ID do projeto não deve ser nulo")
  @NotBlank(message = "O ID do projeto não deve estar em branco")
  String projectId
) {}
