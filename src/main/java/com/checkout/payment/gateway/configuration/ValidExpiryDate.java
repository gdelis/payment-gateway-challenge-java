package com.checkout.payment.gateway.configuration;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Custom validator checking if the card expiry date is in the future:
@Documented
@Constraint(validatedBy = ExpiryDateValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidExpiryDate {
  String message() default "Expiry date must be in the future";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
