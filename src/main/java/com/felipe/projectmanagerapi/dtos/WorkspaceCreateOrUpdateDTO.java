package com.felipe.projectmanagerapi.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record WorkspaceCreateOrUpdateDTO(
  @NotNull(message = "O nome do workspace não deve ser nulo")
  @NotBlank(message = "O nome não deve estar em branco")
  @Length(min = 1, max = 100, message = "O nome deve ter entre 1 e 100 caracteres")
  String name
) {}
