package com.felipe.projectmanagerapi.dtos.mappers;

import com.felipe.projectmanagerapi.dtos.UserResponseDTO;
import com.felipe.projectmanagerapi.enums.Role;
import com.felipe.projectmanagerapi.models.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  public UserResponseDTO toDTO(User user) {
    if(user == null) return null;

    return new UserResponseDTO(
      user.getId(),
      user.getName(),
      user.getEmail(),
      user.getRole().getName(),
      user.getCreatedAt(),
      user.getUpdatedAt()
    );
  }

  public Role convertValueToRole(String value) throws IllegalArgumentException {
    if(value == null) return null;

    return switch(value.toUpperCase()) {
      case "ADMIN" -> Role.ADMIN;
      case "WRITE_READ" -> Role.WRITE_READ;
      case "READ_ONLY" -> Role.READ_ONLY;
      default -> throw new IllegalArgumentException("Não foi possível converter '" + value + "' para Role");
    };
  }
}
