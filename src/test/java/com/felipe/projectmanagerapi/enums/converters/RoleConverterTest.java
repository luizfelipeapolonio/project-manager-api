package com.felipe.projectmanagerapi.enums.converters;

import com.felipe.projectmanagerapi.enums.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;

public class RoleConverterTest {

  @Spy
  RoleConverter roleConverter;

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
  @DisplayName("convertToDatabaseColumn - Should successfully convert a given Role value to its equivalent string value")
  void convertRoleToDatabaseColumnStringSuccess() {
    String admin = this.roleConverter.convertToDatabaseColumn(Role.ADMIN);
    String writeRead = this.roleConverter.convertToDatabaseColumn(Role.WRITE_READ);
    String readOnly = this.roleConverter.convertToDatabaseColumn(Role.READ_ONLY);

    assertThat(admin).isEqualTo("ADMIN");
    assertThat(writeRead).isEqualTo("WRITE_READ");
    assertThat(readOnly).isEqualTo("READ_ONLY");
  }

  @Test
  @DisplayName("convertToDatabaseColumn - Should return null if the given argument is a null value")
  void convertRoleToDatabaseColumnStringFailsByNullArgument() {
    String convertedRole = this.roleConverter.convertToDatabaseColumn(null);
    assertThat(convertedRole).isNull();
  }

  @Test
  @DisplayName("convertToEntityAttribute - Should successfully convert a given string to its equivalent Enum Role value")
  void convertStringToEnumRoleEntityAttributeSuccess() {
    Role admin = this.roleConverter.convertToEntityAttribute("ADMIN");
    Role writeRead = this.roleConverter.convertToEntityAttribute("WRITE_READ");
    Role readOnly = this.roleConverter.convertToEntityAttribute("READ_ONLY");

    assertThat(admin).isEqualTo(Role.ADMIN);
    assertThat(writeRead).isEqualTo(Role.WRITE_READ);
    assertThat(readOnly).isEqualTo(Role.READ_ONLY);
  }

  @Test
  @DisplayName("convertToEntityAttribute - Should throw an IllegalArgumentException when an invalid string value is given")
  void convertStringToEnumRoleEntityAttributeFailsByInvalidStringValue() {
    String value = "RANDOM_VALUE";
    Exception thrown = catchException(() -> this.roleConverter.convertToEntityAttribute(value));

    assertThat(thrown)
      .isExactlyInstanceOf(IllegalArgumentException.class)
      .hasMessage("Valor do Enum inválido: " + value);
  }

  @Test
  @DisplayName("convertToEntityAttribute - Should return null if the given argument is a null value")
  void convertStringToEnumRoleEntityAttributeFailsByNullArgument() {
    Role convertedRole = this.roleConverter.convertToEntityAttribute(null);
    assertThat(convertedRole).isNull();
  }

}
