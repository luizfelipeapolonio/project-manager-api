package com.felipe.projectmanagerapi.enums.converters;

import com.felipe.projectmanagerapi.enums.Role;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

@Converter
public class RoleConverter implements AttributeConverter<Role, String> {
  @Override
  public String convertToDatabaseColumn(Role role) {
    if(role == null) return null;
    return role.getName().toUpperCase();
  }

  @Override
  public Role convertToEntityAttribute(String name) {
    if(name == null) return null;

    return Stream.of(Role.values())
      .filter(role -> role.getName().equals(name))
      .findFirst()
      .orElseThrow(() -> new IllegalArgumentException("Valor do Enum inválido: " + name));
  }
}
