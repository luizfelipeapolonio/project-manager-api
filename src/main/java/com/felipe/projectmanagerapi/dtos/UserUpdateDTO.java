package com.felipe.projectmanagerapi.dtos;

import jakarta.annotation.Nullable;
import org.hibernate.validator.constraints.Length;

public record UserUpdateDTO(
  @Nullable
  @Length(min = 1, max = 20, message = "O nome deve ter entre 1 e 20 caracteres")
  String name,

  @Nullable
  @Length(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
  String password
) {}
