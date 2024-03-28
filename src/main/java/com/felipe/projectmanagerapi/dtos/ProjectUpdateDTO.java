package com.felipe.projectmanagerapi.dtos;

import com.felipe.projectmanagerapi.enums.validation.ValueOfPriorityLevel;
import com.felipe.projectmanagerapi.utils.ValueOfDeadline;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

public record ProjectUpdateDTO(

  @Nullable
  @Length(min = 1, max = 70, message = "O nome deve conter entre 1 e 70 caracteres")
  String name,

  @Nullable
  @Length(min = 1, max = 40, message = "A categoria deve ter entre 1 e 40 caracteres")
  String category,

  @Nullable
  String description,

  @Nullable
  @Pattern(regexp = "^\\d+\\.\\d{2}$", message = "Custo inválido! Digite no formato válido. Ex: 1200.00")
  String budget,

  @Nullable
  @ValueOfPriorityLevel
  String priority,

  @Nullable
  @ValueOfDeadline
  String deadline
) {}
