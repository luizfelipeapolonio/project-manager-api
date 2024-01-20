package com.felipe.projectmanagerapi.enums.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;
import java.util.stream.Stream;

public class ValueOfRoleValidator implements ConstraintValidator<ValueOfRole, String> {
  private List<String> acceptedValues;

  @Override
  public void initialize(ValueOfRole annotation) {
    this.acceptedValues = Stream.of(annotation.enumClass().getEnumConstants())
      .map(Enum::name)
      .toList();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if(value == null || value.isEmpty()) return true;
    return this.acceptedValues.contains(value.toUpperCase());
  }
}
