package com.felipe.projectmanagerapi.dtos;

import com.felipe.projectmanagerapi.enums.validation.ValueOfPriorityLevel;
import com.felipe.projectmanagerapi.utils.ValueOfDeadline;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.DecimalMin;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

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
  @DecimalMin(value = "0.00", message = "O valor mínimo aceito é 0.00")
  BigDecimal budget,

  @Nullable
  @ValueOfPriorityLevel
  String priority,

  @Nullable
  @ValueOfDeadline
  String deadline
) {}
