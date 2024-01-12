package com.felipe.projectmanagerapi.dtos.mappers;

import com.felipe.projectmanagerapi.dtos.UserResponseDTO;
import com.felipe.projectmanagerapi.enums.Role;
import com.felipe.projectmanagerapi.models.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;

public class UserMapperTest {

  @Spy
  UserMapper userMapper;

  private AutoCloseable closeable;

  @BeforeEach
  void setUp() {
    this.closeable = MockitoAnnotations.openMocks(this);
  }

  @AfterEach
  void tearDown() throws Exception {
    this.closeable.close();
  }

  @Test
  @DisplayName("userMapper - Should convert successfully a User object to a UserResponseDTO")
  void convertToUserResponseDTOSuccess() {
    User user = new User(
      "01",
      "User 1",
      "user1@email.com",
      "123456",
      Role.READ_ONLY
    );

    UserResponseDTO convertedResponse = this.userMapper.toDTO(user);

    assertThat(convertedResponse.id()).isEqualTo(user.getId());
    assertThat(convertedResponse.name()).isEqualTo(user.getName());
    assertThat(convertedResponse.email()).isEqualTo(user.getEmail());
    assertThat(convertedResponse.role()).isEqualTo(user.getRole().getName());
    assertThat(convertedResponse.createdAt()).isEqualTo(user.getCreatedAt());
    assertThat(convertedResponse.updatedAt()).isEqualTo(user.getUpdatedAt());

    verify(this.userMapper, times(1)).toDTO(any(User.class));
  }

  @Test
  @DisplayName("userMapper - Should convert successfully the string value to one of the roles")
  void convertValueToRoleSuccess() {
    String value = "READ_ONLY";
    Role convertedRole = this.userMapper.convertValueToRole(value);
    assertThat(convertedRole.getName()).isEqualTo(value);
  }

  @Test
  @DisplayName("userMapper - Should throw an IllegalArgumentException if an invalid value is given")
  void convertValueToRoleFailsByInvalidValue() {
    String value = "RandomValue";
    Exception thrown = catchException(() -> this.userMapper.convertValueToRole(value));

    assertThat(thrown)
      .isExactlyInstanceOf(IllegalArgumentException.class)
      .hasMessage("Não foi possível converter '" + value + "' para Role");

    verify(this.userMapper, times(1)).convertValueToRole(anyString());
  }
}
