package com.felipe.projectmanagerapi.dtos;

import com.felipe.projectmanagerapi.enums.Role;
import com.felipe.projectmanagerapi.enums.validation.ValueOfRole;
import jakarta.validation.constraints.NotNull;

public record UserRoleUpdateDTO(
  @NotNull(message = "A role não deve ser nula")
  @ValueOfRole(enumClass = Role.class)
  String role
) {}
