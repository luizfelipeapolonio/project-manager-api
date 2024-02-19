package com.felipe.projectmanagerapi.enums.validation;

import com.felipe.projectmanagerapi.enums.PriorityLevel;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;
import java.util.stream.Stream;

public class ValueOfPriorityLevelValidator implements ConstraintValidator<ValueOfPriorityLevel, String> {
  List<String> acceptedValues;

  @Override
  public void initialize(ValueOfPriorityLevel constraintAnnotation) {
    this.acceptedValues = Stream.of(constraintAnnotation.enumClass().getEnumConstants())
      .map(PriorityLevel::getValue)
      .toList();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if(value == null || value.isEmpty()) return true;
    return this.acceptedValues.contains(value);
  }
}
