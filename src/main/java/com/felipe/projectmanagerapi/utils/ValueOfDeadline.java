package com.felipe.projectmanagerapi.utils;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValueOfDeadlineValidator.class)
public @interface ValueOfDeadline {
  String message() default "Data inválida. Formato aceito: dd-MM-yyyy. Ex:. 01-01-2024";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}
