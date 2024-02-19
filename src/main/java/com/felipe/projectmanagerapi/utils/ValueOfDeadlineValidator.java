package com.felipe.projectmanagerapi.utils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ValueOfDeadlineValidator implements ConstraintValidator<ValueOfDeadline, String> {
  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if(value == null || value.isEmpty()) return true;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    try {
      LocalDate.parse(value, formatter);
    } catch(DateTimeParseException e) {
      return false;
    }
    return true;
  }
}
