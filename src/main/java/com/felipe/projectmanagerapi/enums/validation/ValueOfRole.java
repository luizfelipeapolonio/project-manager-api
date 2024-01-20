package com.felipe.projectmanagerapi.enums.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Documented;
import java.lang.annotation.RetentionPolicy;

@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValueOfRoleValidator.class)
public @interface ValueOfRole {
  Class<? extends Enum<?>> enumClass();
  String message() default "Os valores aceitos são: ADMIN, WRITE_READ, READ_ONLY";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}
