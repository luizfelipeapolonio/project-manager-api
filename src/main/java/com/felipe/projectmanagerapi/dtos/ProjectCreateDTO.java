package com.felipe.projectmanagerapi.dtos;

import com.felipe.projectmanagerapi.utils.ValueOfDeadline;
import com.felipe.projectmanagerapi.enums.validation.ValueOfPriorityLevel;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

public record ProjectCreateDTO(

  @NotNull(message = "O nome do projeto não deve ser nulo")
  @NotBlank(message = "O nome do projeto não deve estar em branco")
  @Length(min = 1, max = 70, message = "O nome deve conter entre 1 e 70 caracteres")
  String name,

  @NotNull(message = "A categoria não pode ser nula")
  @NotBlank(message = "A categoria não deve estar em branco")
  @Length(min = 1, max = 40, message = "A categoria deve ter entre 1 e 40 caracteres")
  String category,

  @NotNull(message = "A descrição não deve ser nula")
  @NotBlank(message = "A descrição não deve estar em branco")
  String description,

  @NotNull(message = "O orçamento não deve ser nulo")
  @DecimalMin(value = "0.00", message = "O valor mínimo aceito é 0.0")
  BigDecimal budget,

  @ValueOfPriorityLevel
  String priority,

  @NotNull(message = "O prazo não deve ser nulo")
  @NotBlank(message = "O prazo não deve estar em branco")
  @ValueOfDeadline
  String deadline,

  @NotNull(message = "O ID do workspace não deve ser nulo")
  @NotBlank(message = "O ID do workspace não estar em branco")
  String workspaceId
) {}
