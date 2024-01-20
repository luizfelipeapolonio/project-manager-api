package com.felipe.projectmanagerapi.dtos;

import com.felipe.projectmanagerapi.enums.Role;
import com.felipe.projectmanagerapi.enums.validation.ValueOfRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record UserRegisterDTO(
  @NotNull(message = "O nome é obrigatório")
  @NotBlank(message = "O nome não deve estar em branco")
  @Length(min = 1, max = 20, message = "O nome deve ter entre 1 e 20 caracteres")
  String name,

  @NotNull(message = "O e-mail é obrigatório")
  @NotBlank(message = "O e-mail não deve estar em branco")
  @Email(message = "O e-mail deve ser um e-mail válido")
  String email,

  @NotNull(message = "A senha é obrigatória")
  @NotBlank(message = "A senha não deve estar em branco")
  @Length(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
  String password,

  @NotNull(message = "A role é obrigatória")
  @NotBlank(message = "A role não deve estar em branco")
  @ValueOfRole(enumClass = Role.class)
  String role
) {}
