package com.felipe.projectmanagerapi.enums.validation;

import com.felipe.projectmanagerapi.enums.PriorityLevel;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValueOfPriorityLevelValidator.class)
public @interface ValueOfPriorityLevel {
  Class<PriorityLevel> enumClass() default PriorityLevel.class;
  String message() default "Os valores aceitos são: alta, media, baixa";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}
