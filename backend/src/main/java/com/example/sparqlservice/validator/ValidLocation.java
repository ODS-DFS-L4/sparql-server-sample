package com.example.sparqlservice.validator;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = LocationValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidLocation {
  String message() default "Invalid location: both latitude and longitude must be null or both must be non-null";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}